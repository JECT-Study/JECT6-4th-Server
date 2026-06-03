package com.ject6.boost.domain.user.presentation.controller;

import com.ject6.boost.common.dto.ApiResponse;
import com.ject6.boost.common.security.AuthenticatedUser;
import com.ject6.boost.domain.user.application.service.OnboardingService;
import com.ject6.boost.domain.user.presentation.dto.OnboardingProfileRequest;
import com.ject6.boost.domain.user.presentation.dto.OnboardingProfileResponse;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/onboarding")
@RequiredArgsConstructor
public class OnboardingController {

    private final OnboardingService onboardingService;

    @Operation(summary = "온보딩 정보 입력", description = "현재 로그인한 사용자의 닉네임, 관심 카테고리, 활동 유형, 지역 정보를 저장하고 온보딩을 완료합니다.")
    @PostMapping("/profile")
    public ApiResponse<OnboardingProfileResponse> updateProfile(
            @AuthenticationPrincipal AuthenticatedUser principal,
            @RequestBody OnboardingProfileRequest request
    ) {
        return ApiResponse.success(onboardingService.updateProfile(principal, request));
    }
}
