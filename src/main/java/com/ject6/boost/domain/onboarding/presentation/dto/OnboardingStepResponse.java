package com.ject6.boost.domain.onboarding.presentation.dto;

public record OnboardingStepResponse(
        String sessionId,
        int step,
        boolean isComplete,
        Integer nextStep
) {}
