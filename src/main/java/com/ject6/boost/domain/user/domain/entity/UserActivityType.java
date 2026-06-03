package com.ject6.boost.domain.user.domain.entity;

import com.ject6.boost.domain.user.domain.constant.ActivityType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.OffsetDateTime;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

@Getter
@Entity
@Table(name = "user_activity_types")
@IdClass(UserActivityTypeId.class)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserActivityType {

    @Id
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Id
    @Enumerated(EnumType.STRING)
    @Column(name = "activity_type", nullable = false, length = 50)
    private ActivityType activityType;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @Column(name = "deleted_at")
    private OffsetDateTime deletedAt;

    /**
     * 사용자와 활동 유형의 선택 관계를 생성하는 함수.
     */
    public static UserActivityType create(User user, ActivityType activityType) {
        UserActivityType userActivityType = new UserActivityType();
        userActivityType.user = user;
        userActivityType.activityType = activityType;
        return userActivityType;
    }
}
