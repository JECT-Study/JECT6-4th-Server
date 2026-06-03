package com.ject6.boost.domain.user.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.OffsetDateTime;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

@Getter
@Entity
@Table(name = "users")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "nickname", length = 100)
    private String nickname;

    @Column(name = "onboarding_completed", nullable = false)
    private boolean onboardingCompleted = false;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    @Column(name = "deleted_at")
    private OffsetDateTime deletedAt;

    /**
     * 온보딩 전 기본 사용자 엔티티를 생성하는 함수.
     */
    public static User create() {
        return new User();
    }

    /**
     * 온보딩 입력 닉네임을 저장하고 온보딩 완료 상태로 변경하는 함수.
     */
    public void completeOnboarding(String nickname) {
        this.nickname = nickname;
        this.onboardingCompleted = true;
    }

    /**
     * 사용자 프로필 닉네임을 수정하는 함수.
     */
    public void updateNickname(String nickname) {
        this.nickname = nickname;
    }

    /**
     * 사용자를 탈퇴 상태로 변경하는 함수.
     */
    public void withdraw(OffsetDateTime deletedAt) {
        this.deletedAt = deletedAt;
    }
}
