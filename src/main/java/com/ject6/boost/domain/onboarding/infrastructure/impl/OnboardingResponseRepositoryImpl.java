package com.ject6.boost.domain.onboarding.infrastructure.impl;

import com.ject6.boost.domain.onboarding.domain.entity.OnboardingResponse;
import com.ject6.boost.domain.onboarding.domain.repository.OnboardingResponseRepository;
import com.ject6.boost.domain.onboarding.infrastructure.repository.OnboardingResponseJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class OnboardingResponseRepositoryImpl implements OnboardingResponseRepository {
    private final OnboardingResponseJpaRepository jpa;
    @Override public OnboardingResponse save(OnboardingResponse r) { return jpa.save(r); }
    @Override public Optional<OnboardingResponse> findBySessionId(String s) { return jpa.findBySessionId(s); }
}
