package com.ject6.boost.domain.auth.application.controller;

import com.ject6.boost.common.dto.ApiResponse;
import com.ject6.boost.domain.auth.application.dto.TokenRefreshResponse;
import com.ject6.boost.domain.auth.application.handler.OAuth2LoginSuccessHandler;
import com.ject6.boost.domain.auth.domain.OAuthProvider;
import com.ject6.boost.domain.auth.presentaion.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.servlet.http.HttpServletResponse;
import java.time.Duration;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.view.RedirectView;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private static final String BEARER_PREFIX = "Bearer ";

    private final AuthService authService;

    @Operation(summary = "소셜 로그인", description = "제공자(KAKAO/NAVER/GOOGLE)에 해당하는 소셜 로그인 페이지로 이동합니다.")
    @GetMapping("/login/{provider}")
    public RedirectView login(@PathVariable String provider) {
        OAuthProvider oauthProvider = OAuthProvider.from(provider);
        return new RedirectView("/oauth2/authorization/" + oauthProvider.registrationId());
    }

    @Operation(summary = "로그아웃", description = "현재 액세스 토큰 세션과 리프레시 토큰 세션을 삭제하고 리프레시 토큰 쿠키를 만료합니다.")
    @PostMapping("/logout")
    public ApiResponse<Void> logout(
            @RequestHeader(name = HttpHeaders.AUTHORIZATION, required = false) String authorization,
            @CookieValue(name = OAuth2LoginSuccessHandler.REFRESH_TOKEN_COOKIE_NAME, required = false) String refreshToken,
            HttpServletResponse response
    ) {
        authService.logout(extractBearerToken(authorization), refreshToken);
        response.addHeader(HttpHeaders.SET_COOKIE, expireRefreshTokenCookie().toString());
        return ApiResponse.success(null);
    }

    @Operation(summary = "액세스 토큰 재발급", description = "쿠키에 저장된 리프레시 토큰으로 새 액세스 토큰을 발급합니다.")
    @PostMapping("/refresh")
    public ApiResponse<TokenRefreshResponse> refresh(
            @CookieValue(name = OAuth2LoginSuccessHandler.REFRESH_TOKEN_COOKIE_NAME, required = false) String refreshToken
    ) {
        return ApiResponse.success(authService.refresh(refreshToken));
    }

    private String extractBearerToken(String authorization) {
        if (authorization == null || !authorization.startsWith(BEARER_PREFIX)) {
            return null;
        }
        return authorization.substring(BEARER_PREFIX.length());
    }

    private ResponseCookie expireRefreshTokenCookie() {
        return ResponseCookie.from(OAuth2LoginSuccessHandler.REFRESH_TOKEN_COOKIE_NAME, "")
                .httpOnly(true)
                .secure(false)
                .sameSite("Lax")
                .path("/api/auth/refresh")
                .maxAge(Duration.ZERO)
                .build();
    }
}
