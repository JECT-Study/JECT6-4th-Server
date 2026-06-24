package com.ject6.boost.application.blog.service;

import com.ject6.boost.application.common.exception.BusinessException;
import com.ject6.boost.domain.blog.entity.DiagnosisQuota;
import com.ject6.boost.domain.blog.repository.DiagnosisQuotaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DiagnosisQuotaServiceTest {

    @Mock private DiagnosisQuotaRepository quotaRepository;

    private DiagnosisQuotaService sut;

    @BeforeEach
    void setUp() {
        sut = new DiagnosisQuotaService(quotaRepository);
    }

    // ── reserveOrThrow ────────────────────────────────────────────────────────

    @Test
    @DisplayName("첫 진단 요청 — 쿼터가 없으면 default 생성 후 예약")
    void reserveOrThrow_createsDefaultAndReserves() {
        given(quotaRepository.findByUserId(1L)).willReturn(Optional.empty());
        given(quotaRepository.save(any())).willAnswer(inv -> inv.getArgument(0));

        sut.reserveOrThrow(1L);

        ArgumentCaptor<DiagnosisQuota> cap = ArgumentCaptor.forClass(DiagnosisQuota.class);
        verify(quotaRepository, times(1)).save(cap.capture());
        DiagnosisQuota saved = cap.getValue();
        assertThat(saved.getReserved()).isEqualTo(1);
        assertThat(saved.getResetAt()).isNotNull();
    }

    @Test
    @DisplayName("한도 초과 — BusinessException 발생")
    void reserveOrThrow_throwsWhenLimitExceeded() {
        DiagnosisQuota full = DiagnosisQuota.createDefault(1L);
        // 3개 모두 사용 처리
        full.reserve(); full.confirmUsed("c1");
        full.reserve(); full.confirmUsed("c2");
        full.reserve(); full.confirmUsed("c3");
        given(quotaRepository.findByUserId(1L)).willReturn(Optional.of(full));

        assertThatThrownBy(() -> sut.reserveOrThrow(1L))
                .isInstanceOf(BusinessException.class);
    }

    // ── confirmUsed ──────────────────────────────────────────────────────────

    @Test
    @DisplayName("B-2: 동일 correlationId로 재호출 시 used 중복 차감 방지")
    void confirmUsed_idempotent() {
        DiagnosisQuota q = DiagnosisQuota.createDefault(1L);
        q.reserve();
        given(quotaRepository.findByUserId(1L)).willReturn(Optional.of(q));
        given(quotaRepository.save(any())).willAnswer(inv -> inv.getArgument(0));

        sut.confirmUsed(1L, "corr-1");
        sut.confirmUsed(1L, "corr-1");

        assertThat(q.getUsed()).isEqualTo(1);
    }

    @Test
    @DisplayName("서로 다른 correlationId — 각각 차감")
    void confirmUsed_distinctCorrelationIds() {
        DiagnosisQuota q = DiagnosisQuota.createDefault(1L);
        q.reserve(); q.reserve();
        given(quotaRepository.findByUserId(1L)).willReturn(Optional.of(q));
        given(quotaRepository.save(any())).willAnswer(inv -> inv.getArgument(0));

        sut.confirmUsed(1L, "corr-1");
        sut.confirmUsed(1L, "corr-2");

        assertThat(q.getUsed()).isEqualTo(2);
    }

    // ── D-2: reset_at 초기화 ─────────────────────────────────────────────────

    @Test
    @DisplayName("D-2: createDefault 시 resetAt이 다음 달로 설정됨")
    void createDefault_initializesResetAt() {
        DiagnosisQuota q = DiagnosisQuota.createDefault(99L);
        assertThat(q.getResetAt()).isNotNull();
    }

    @Test
    @DisplayName("D-2: resetIfExpired 후 resetAt이 다음 달로 재설정됨")
    void resetIfExpired_reinitializesResetAt() {
        DiagnosisQuota q = DiagnosisQuota.createDefault(99L);
        // 만료된 것처럼 과거 날짜 설정
        q.setResetAt(java.time.OffsetDateTime.now().minusDays(1));
        q.resetIfExpired();
        assertThat(q.getResetAt()).isAfter(java.time.OffsetDateTime.now());
        assertThat(q.getUsed()).isEqualTo(0);
        assertThat(q.getReserved()).isEqualTo(0);
    }
}
