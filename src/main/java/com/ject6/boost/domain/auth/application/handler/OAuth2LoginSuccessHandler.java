package com.ject6.boost.domain.auth.application.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ject6.boost.common.dto.ApiResponse;
import com.ject6.boost.common.exception.BusinessException;
import com.ject6.boost.common.security.SecurityErrorResponseWriter;
import com.ject6.boost.domain.auth.application.dto.OAuthLoginResult;
import com.ject6.boost.domain.auth.application.dto.OAuthLoginResponse;
import com.ject6.boost.domain.auth.domain.OAuthProvider;
import com.ject6.boost.domain.auth.presentaion.exception.AuthErrorCode;
import com.ject6.boost.domain.auth.presentaion.service.AuthService;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.time.Duration;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseCookie;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class OAuth2LoginSuccessHandler implements AuthenticationSuccessHandler {

    public static final String REFRESH_TOKEN_COOKIE_NAME = "refresh_token";

    private final AuthService authService;
    private final ObjectMapper objectMapper;
    private final SecurityErrorResponseWriter securityErrorResponseWriter;

    /**
     * OAuth2 로그인 성공 후 서비스 access token을 발급하고 JSON 응답을 반환하는 함수.
     */
    @Override
    public void onAuthenticationSuccess(
            HttpServletRequest request,
            HttpServletResponse response,
            Authentication authentication
    ) throws IOException, ServletException {
        try {
            LoginIdentity loginIdentity = toLoginIdentity(authentication);
            OAuthLoginResult loginResult = authService.login(
                    loginIdentity.provider(),
                    loginIdentity.providerUserId()
            );
            OAuthLoginResponse loginResponse = loginResult.response();

            HttpSession session = request.getSession(false);
            if (session != null) {
                session.invalidate();
            }
            SecurityContextHolder.clearContext();

            response.setStatus(HttpServletResponse.SC_OK);
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            response.setCharacterEncoding("UTF-8");
            response.addHeader(HttpHeaders.SET_COOKIE, createRefreshTokenCookie(loginResult).toString());
            objectMapper.writeValue(response.getWriter(), ApiResponse.success(loginResponse));
        } catch (BusinessException exception) {
            securityErrorResponseWriter.write(response, exception.getErrorCode());
        }
    }

    /**
     * refresh token을 HttpOnly 쿠키로 전달하기 위한 Set-Cookie 값을 생성하는 함수.
     */
    private ResponseCookie createRefreshTokenCookie(OAuthLoginResult loginResult) {
        return ResponseCookie.from(REFRESH_TOKEN_COOKIE_NAME, loginResult.refreshToken())
                .httpOnly(true)
                .secure(false)
                .sameSite("Lax")
                .path("/api/auth/refresh")
                .maxAge(Duration.ofSeconds(loginResult.refreshTokenExpiresIn()))
                .build();
    }

    /**
     * Spring Security OAuth2 인증 결과에서 서비스 로그인에 필요한 provider 식별자를 추출하는 함수.
     */
    private LoginIdentity toLoginIdentity(Authentication authentication) {
        if (!(authentication instanceof OAuth2AuthenticationToken oauth2Authentication)
                || !(authentication.getPrincipal() instanceof OAuth2User oauth2User)) {
            throw new BusinessException(AuthErrorCode.UNSUPPORTED_OAUTH_PROVIDER);
        }

        OAuthProvider provider = OAuthProvider.fromRegistrationId(
                oauth2Authentication.getAuthorizedClientRegistrationId()
        );

        Object id = oauth2User.getAttribute("id");
        if (id == null) {
            throw new BusinessException(AuthErrorCode.OAUTH_USER_ID_MISSING);
        }

        return new LoginIdentity(
                provider,
                String.valueOf(id)
        );
    }

    private record LoginIdentity(
            OAuthProvider provider,
            String providerUserId
    ) {
    }
}
