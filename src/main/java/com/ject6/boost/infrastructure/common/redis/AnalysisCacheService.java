package com.ject6.boost.infrastructure.common.redis;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Optional;

/**
 * 분석 결과 Redis 포인터 캐시 + 중복 요청 lock.
 *
 * 키 형식:
 *   분석 캐시  analysis-cache:FULL_BLOG:{userId}:{blogId}:v1:default
 *             analysis-cache:POST:{userId}:{documentId}:v1:default
 *   분석 lock  analysis-lock:FULL_BLOG:{userId}:{blogId}
 *   분석 ctx   analysis-ctx:{correlationId}
 *
 * 캐시 값: analysisJobId (String)
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AnalysisCacheService {

    private static final String CACHE_PREFIX   = "analysis-cache:";
    private static final String LOCK_PREFIX    = "analysis-lock:";
    private static final String CTX_PREFIX     = "analysis-ctx:";
    private static final String IDEM_PREFIX    = "completed-event:processed:";
    private static final Duration CACHE_TTL    = Duration.ofHours(24);
    private static final Duration LOCK_TTL     = Duration.ofMinutes(35);
    private static final Duration CTX_TTL      = Duration.ofMinutes(35);
    private static final Duration IDEM_TTL     = Duration.ofDays(7);

    private final RedisTemplate<String, String> redisTemplate;

    // ── 캐시 ──────────────────────────────────────────────────────────────────

    public Optional<String> getFullBlogCache(Long userId, Long blogId) {
        String key = cacheKey("FULL_BLOG", userId, blogId);
        return Optional.ofNullable(redisTemplate.opsForValue().get(key));
    }

    public Optional<String> getPostCache(Long userId, Long documentId) {
        String key = cacheKey("POST", userId, documentId);
        return Optional.ofNullable(redisTemplate.opsForValue().get(key));
    }

    public void putFullBlogCache(Long userId, Long blogId, Long analysisJobId) {
        redisTemplate.opsForValue().set(
                cacheKey("FULL_BLOG", userId, blogId),
                String.valueOf(analysisJobId),
                CACHE_TTL
        );
        log.debug("analysis cache stored FULL_BLOG userId={} blogId={} jobId={}", userId, blogId, analysisJobId);
    }

    public void putPostCache(Long userId, Long documentId, Long analysisJobId) {
        redisTemplate.opsForValue().set(
                cacheKey("POST", userId, documentId),
                String.valueOf(analysisJobId),
                CACHE_TTL
        );
        log.debug("analysis cache stored POST userId={} documentId={} jobId={}", userId, documentId, analysisJobId);
    }

    public void evictFullBlogCache(Long userId, Long blogId) {
        redisTemplate.delete(cacheKey("FULL_BLOG", userId, blogId));
    }

    // ── 중복 요청 lock (의미 기반 키) ─────────────────────────────────────────

    /** FULL_BLOG lock 키: (userId, blogId) 기반 의미 식별자 */
    public String fullBlogLockKey(Long userId, Long blogId) {
        return LOCK_PREFIX + "FULL_BLOG:" + userId + ":" + blogId;
    }

    /** 이미 진행 중인 동일 (userId, blogId) FULL_BLOG 분석이 있으면 false 반환. */
    public boolean acquireFullBlogLock(Long userId, Long blogId) {
        Boolean ok = redisTemplate.opsForValue()
                .setIfAbsent(fullBlogLockKey(userId, blogId), "1", LOCK_TTL);
        return Boolean.TRUE.equals(ok);
    }

    public void releaseFullBlogLock(Long userId, Long blogId) {
        redisTemplate.delete(fullBlogLockKey(userId, blogId));
    }

    // ── correlationId 컨텍스트 (lock 키 역산용) ───────────────────────────────

    /** correlationId → {userId}:{blogId} 매핑을 35분 TTL로 저장. 완료 이벤트에서 lock 해제 시 사용. */
    public void storeCorrelationContext(String correlationId, Long userId, Long blogId) {
        redisTemplate.opsForValue().set(
                CTX_PREFIX + correlationId,
                userId + ":" + blogId,
                CTX_TTL
        );
    }

    /** correlationId로 저장된 [userId, blogId] 배열을 반환. 없으면 null. */
    public long[] getCorrelationContext(String correlationId) {
        String val = redisTemplate.opsForValue().get(CTX_PREFIX + correlationId);
        if (val == null) return null;
        String[] parts = val.split(":");
        if (parts.length != 2) return null;
        try {
            return new long[]{Long.parseLong(parts[0]), Long.parseLong(parts[1])};
        } catch (NumberFormatException e) {
            return null;
        }
    }

    public void deleteCorrelationContext(String correlationId) {
        redisTemplate.delete(CTX_PREFIX + correlationId);
    }

    // ── 이벤트 멱등성 (중복 수신 방어) ──────────────────────────────────────

    /** correlationId가 처음 수신이면 true, 이미 처리된 이벤트면 false. */
    public boolean markEventProcessed(String correlationId) {
        Boolean ok = redisTemplate.opsForValue()
                .setIfAbsent(IDEM_PREFIX + correlationId, "1", IDEM_TTL);
        return Boolean.TRUE.equals(ok);
    }

    // ── 내부 ─────────────────────────────────────────────────────────────────

    private String cacheKey(String mode, Long userId, Long id) {
        return CACHE_PREFIX + mode + ":" + userId + ":" + id + ":v1:default";
    }
}
