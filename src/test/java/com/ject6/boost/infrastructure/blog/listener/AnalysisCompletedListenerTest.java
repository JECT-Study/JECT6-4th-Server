package com.ject6.boost.infrastructure.blog.listener;

import com.ject6.boost.application.blog.service.DiagnosisQuotaService;
import com.ject6.boost.infrastructure.common.queue.AnalysisCompletedMessage;
import com.ject6.boost.infrastructure.common.redis.AnalysisCacheService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AnalysisCompletedListenerTest {

    @Mock private AnalysisCacheService cacheService;
    @Mock private DiagnosisQuotaService quotaService;

    private AnalysisCompletedListener sut;

    @BeforeEach
    void setUp() {
        sut = new AnalysisCompletedListener(cacheService, quotaService);
    }

    private AnalysisCompletedMessage msg(
            Long userId, Long docId, Long jobId, Long blogId,
            String mode, String correlationId, String status) {
        return new AnalysisCompletedMessage(userId, docId, jobId, mode, blogId, null, correlationId, status);
    }

    // ── B-1: 멱등성 ───────────────────────────────────────────────────────────

    @Test
    @DisplayName("B-1: 동일 correlationId 두 번 수신 시 두 번째는 처리 스킵")
    void handle_duplicateEvent_skipped() {
        given(cacheService.markEventProcessed("c1")).willReturn(true, false);
        given(cacheService.getCorrelationContext("c1")).willReturn(new long[]{1L, 10L});

        AnalysisCompletedMessage m = msg(1L, null, 99L, null, "FULL_BLOG", "c1", "SUCCESS");
        sut.handle(m);
        sut.handle(m);

        // 두 번째는 스킵 — cacheService 호출 횟수 1회
        verify(cacheService, times(1)).putFullBlogCache(anyLong(), anyLong(), anyLong());
    }

    // ── A-2: SUCCESS일 때만 캐시 저장 ────────────────────────────────────────

    @Test
    @DisplayName("A-2: FAILED 이벤트 시 캐시 저장하지 않음")
    void handle_failedEvent_noCacheStore() {
        given(cacheService.markEventProcessed("c2")).willReturn(true);

        sut.handle(msg(1L, null, 99L, null, "FULL_BLOG", "c2", "FAILED"));

        verify(cacheService, never()).putFullBlogCache(anyLong(), anyLong(), anyLong());
        verify(quotaService).releaseReservation(1L);
    }

    @Test
    @DisplayName("A-2: SUCCESS 이벤트 시 캐시 저장 및 쿼터 확정")
    void handle_successEvent_cacheStored() {
        given(cacheService.markEventProcessed("c3")).willReturn(true);
        given(cacheService.getCorrelationContext("c3")).willReturn(new long[]{1L, 10L});

        sut.handle(msg(1L, null, 99L, null, "FULL_BLOG", "c3", "SUCCESS"));

        verify(cacheService).putFullBlogCache(1L, 10L, 99L);
        verify(quotaService).confirmUsed(1L, "c3");
    }

    // ── A-1 + A-3: 시맨틱 lock 해제 ─────────────────────────────────────────

    @Test
    @DisplayName("A-3: Redis 컨텍스트로 lock 해제 — correlationId 기반 역산")
    void handle_releaseSemanticLockViaContext() {
        given(cacheService.markEventProcessed("c4")).willReturn(true);
        given(cacheService.getCorrelationContext("c4")).willReturn(new long[]{5L, 20L});

        sut.handle(msg(5L, null, 1L, null, "FULL_BLOG", "c4", "SUCCESS"));

        verify(cacheService).releaseFullBlogLock(5L, 20L);
        verify(cacheService).deleteCorrelationContext("c4");
    }

    @Test
    @DisplayName("A-3: Redis 컨텍스트 없으면 payload blogId로 lock 해제")
    void handle_releaseSemanticLockViaPayload() {
        given(cacheService.markEventProcessed("c5")).willReturn(true);
        given(cacheService.getCorrelationContext("c5")).willReturn(null);

        sut.handle(msg(5L, null, 1L, 30L, "FULL_BLOG", "c5", "SUCCESS"));

        verify(cacheService).releaseFullBlogLock(5L, 30L);
    }

    // ── POST mode ─────────────────────────────────────────────────────────────

    @Test
    @DisplayName("POST SUCCESS — 포스트 캐시 저장, lock 해제 미호출")
    void handle_postSuccess_postCache() {
        given(cacheService.markEventProcessed("c6")).willReturn(true);

        sut.handle(msg(1L, 50L, 77L, null, "POST", "c6", "SUCCESS"));

        verify(cacheService).putPostCache(1L, 50L, 77L);
        verify(cacheService, never()).releaseFullBlogLock(anyLong(), anyLong());
    }
}
