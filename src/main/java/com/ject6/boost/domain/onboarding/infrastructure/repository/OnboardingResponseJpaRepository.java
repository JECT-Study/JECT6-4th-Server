package com.ject6.boost.domain.onboarding.infrastructure.repository;

import com.ject6.boost.domain.onboarding.domain.entity.OnboardingResponse;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface OnboardingResponseJpaRepository extends JpaRepository<OnboardingResponse, Long> {
    Optional<OnboardingResponse> findBySessionId(String sessionId);
}
