package com.ject6.boost.infrastructure.common.redis;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.time.Duration;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class AnalysisCacheServiceTest {

    @Mock private RedisTemplate<String, String> redisTemplate;
    @Mock private ValueOperations<String, String> valueOps;

    private AnalysisCacheService sut;

    @BeforeEach
    void setUp() {
        given(redisTemplate.opsForValue()).willReturn(valueOps);
        sut = new AnalysisCacheService(redisTemplate);
    }

    @Test
    @DisplayName("FULL_BLOG 캐시 hit — 저장된 jobId 반환")
    void getFullBlogCache_hit() {
        given(valueOps.get("analysis-cache:FULL_BLOG:1:10:v1:default")).willReturn("999");
        Optional<String> result = sut.getFullBlogCache(1L, 10L);
        assertThat(result).contains("999");
    }

    @Test
    @DisplayName("FULL_BLOG 캐시 miss — empty 반환")
    void getFullBlogCache_miss() {
        given(valueOps.get(anyString())).willReturn(null);
        assertThat(sut.getFullBlogCache(1L, 10L)).isEmpty();
    }

    @Test
    @DisplayName("FULL_BLOG 캐시 저장 — 올바른 키와 TTL로 set 호출")
    void putFullBlogCache_storesCorrectKey() {
        sut.putFullBlogCache(1L, 10L, 999L);
        verify(valueOps).set(
                eq("analysis-cache:FULL_BLOG:1:10:v1:default"),
                eq("999"),
                eq(Duration.ofHours(24))
        );
    }

    @Test
    @DisplayName("acquireFullBlogLock — 의미 기반 키로 SETNX 호출")
    void acquireFullBlogLock_usesSemanticKey() {
        given(valueOps.setIfAbsent(
                eq("analysis-lock:FULL_BLOG:1:10"), eq("1"), any(Duration.class)
        )).willReturn(true);
        assertThat(sut.acquireFullBlogLock(1L, 10L)).isTrue();
    }

    @Test
    @DisplayName("acquireFullBlogLock — 이미 lock 존재 시 false 반환")
    void acquireFullBlogLock_alreadyLocked() {
        given(valueOps.setIfAbsent(anyString(), anyString(), any(Duration.class))).willReturn(false);
        assertThat(sut.acquireFullBlogLock(1L, 10L)).isFalse();
    }

    @Test
    @DisplayName("storeCorrelationContext → getCorrelationContext — 저장 후 정상 조회")
    void correlationContext_storeAndGet() {
        given(valueOps.get("analysis-ctx:corr-1")).willReturn("1:10");
        long[] ctx = sut.getCorrelationContext("corr-1");
        assertThat(ctx).isNotNull();
        assertThat(ctx[0]).isEqualTo(1L);
        assertThat(ctx[1]).isEqualTo(10L);
    }

    @Test
    @DisplayName("getCorrelationContext — 없는 경우 null 반환")
    void getCorrelationContext_missing() {
        given(valueOps.get(anyString())).willReturn(null);
        assertThat(sut.getCorrelationContext("no-such")).isNull();
    }

    @Test
    @DisplayName("markEventProcessed — 첫 호출은 true, 두 번째는 false")
    void markEventProcessed_idempotent() {
        given(valueOps.setIfAbsent(
                eq("completed-event:processed:corr-1"), eq("1"), any(Duration.class)
        )).willReturn(true, false);

        assertThat(sut.markEventProcessed("corr-1")).isTrue();
        assertThat(sut.markEventProcessed("corr-1")).isFalse();
    }
}
