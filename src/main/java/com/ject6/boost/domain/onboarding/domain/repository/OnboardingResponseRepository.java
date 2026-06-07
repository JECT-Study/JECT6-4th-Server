package com.ject6.boost.domain.onboarding.domain.repository;

import com.ject6.boost.domain.onboarding.domain.entity.OnboardingResponse;
import java.util.Optional;

public interface OnboardingResponseRepository {
    OnboardingResponse save(OnboardingResponse response);
    Optional<OnboardingResponse> findBySessionId(String sessionId);
}
