package com.ject6.boost.domain.blog.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.TemporalAdjusters;

/**
 * 사용자별 무료 진단 쿼터.
 * 비동기 완료 이벤트 기반 2단계 차감:
 *   - 요청 수락 시  reserved += 1
 *   - 완료 이벤트  reserved -= 1, used += 1
 *   - 실패 이벤트  reserved -= 1
 */
@Getter
@Entity
@Table(name = "diagnosis_quotas",
       uniqueConstraints = @UniqueConstraint(columnNames = "user_id"))
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class DiagnosisQuota {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "used", nullable = false)
    private int used = 0;

    @Column(name = "reserved", nullable = false)
    private int reserved = 0;

    @Column(name = "limit_count", nullable = false)
    private int limitCount = 3;

    @Column(name = "reset_at")
    private OffsetDateTime resetAt;

    /** B-2: 중복 이벤트로 인한 used 중복 차감 방지용 멱등 키. */
    @Column(name = "last_confirmed_correlation_id", length = 64)
    private String lastConfirmedCorrelationId;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    public static DiagnosisQuota createDefault(Long userId) {
        DiagnosisQuota q = new DiagnosisQuota();
        q.userId = userId;
        q.used = 0;
        q.reserved = 0;
        q.limitCount = 3;
        q.resetAt = nextMonthlyReset();
        return q;
    }

    /** D-2: 다음 달 1일 00:00 KST를 반환. */
    public static OffsetDateTime nextMonthlyReset() {
        ZonedDateTime kst = ZonedDateTime.now(ZoneId.of("Asia/Seoul"))
                .with(TemporalAdjusters.firstDayOfNextMonth())
                .toLocalDate()
                .atStartOfDay(ZoneId.of("Asia/Seoul"));
        return kst.toOffsetDateTime();
    }

    /** 이미 reset_at이 경과했으면 초기화하고 다음 reset_at을 설정한다. */
    public void resetIfExpired() {
        if (resetAt != null && OffsetDateTime.now().isAfter(resetAt)) {
            used = 0;
            reserved = 0;
            lastConfirmedCorrelationId = null;
            resetAt = nextMonthlyReset();
        }
    }

    public boolean canReserve() {
        return used + reserved < limitCount;
    }

    public void reserve() {
        reserved++;
    }

    /**
     * B-2: 완료 이벤트 — 예약 해제 + 사용 확정.
     * 동일 correlationId로 재호출 시 used를 중복 차감하지 않는다.
     * @return 실제로 차감이 발생하면 true, 멱등 스킵이면 false
     */
    public boolean confirmUsed(String correlationId) {
        if (correlationId != null && correlationId.equals(this.lastConfirmedCorrelationId)) {
            return false;
        }
        reserved = Math.max(0, reserved - 1);
        used++;
        this.lastConfirmedCorrelationId = correlationId;
        return true;
    }

    /** 실패/타임아웃 이벤트: 예약만 해제. */
    public void releaseReservation() {
        reserved = Math.max(0, reserved - 1);
    }

    public int remaining() {
        return Math.max(0, limitCount - used - reserved);
    }

    /** 테스트 전용: resetAt을 임의 값으로 설정. */
    public void setResetAt(OffsetDateTime resetAt) {
        this.resetAt = resetAt;
    }
}
