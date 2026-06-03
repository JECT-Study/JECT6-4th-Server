package com.ject6.boost.domain.user.domain.entity;

import com.ject6.boost.domain.user.domain.constant.ActivityType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.OffsetDateTime;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

@Getter
@Entity
@Table(name = "user_activity_channels")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserActivityChannel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(name = "activity_type", nullable = false, length = 50)
    private ActivityType activityType;

    @Column(name = "url", nullable = false, columnDefinition = "text")
    private String url;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    @Column(name = "deleted_at")
    private OffsetDateTime deletedAt;

    /**
     * 사용자 활동 채널 URL 엔티티를 생성하는 함수.
     */
    public static UserActivityChannel create(User user, ActivityType activityType, String url) {
        UserActivityChannel channel = new UserActivityChannel();
        channel.user = user;
        channel.activityType = activityType;
        channel.url = url;
        return channel;
    }

    /**
     * 활동 채널 URL을 수정하는 함수.
     */
    public void updateUrl(String url) {
        this.url = url;
    }
}
