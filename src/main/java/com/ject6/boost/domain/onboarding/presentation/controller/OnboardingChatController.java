package com.ject6.boost.domain.onboarding.presentation.controller;

import com.ject6.boost.common.dto.ApiResponse;
import com.ject6.boost.domain.onboarding.application.service.OnboardingChatService;
import com.ject6.boost.domain.onboarding.presentation.controller.docs.OnboardingChatApi;
import com.ject6.boost.domain.onboarding.presentation.dto.OnboardingRecommendResponse;
import com.ject6.boost.domain.onboarding.presentation.dto.OnboardingStepRequest;
import com.ject6.boost.domain.onboarding.presentation.dto.OnboardingStepResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/onboarding")
@RequiredArgsConstructor
public class OnboardingChatController implements OnboardingChatApi {

    private final OnboardingChatService onboardingChatService;

    @PostMapping("/response")
    @Override
    public ApiResponse<OnboardingStepResponse> saveStep(@Valid @RequestBody OnboardingStepRequest request) {
        return ApiResponse.success(onboardingChatService.saveStep(request));
    }

    @GetMapping("/recommendations")
    @Override
    public ApiResponse<OnboardingRecommendResponse> getRecommendations(@RequestParam String sessionId) {
        return ApiResponse.success(onboardingChatService.getRecommendations(sessionId));
    }
}
