package com.ject6.boost.domain.auth.presentaion.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ject6.boost.common.exception.BusinessException;
import com.ject6.boost.common.security.AuthenticatedUser;
import com.ject6.boost.common.security.JwtToken;
import com.ject6.boost.common.security.JwtTokenProvider;
import com.ject6.boost.domain.auth.application.dto.OAuthLoginResult;
import com.ject6.boost.domain.auth.application.dto.OAuthLoginResponse;
import com.ject6.boost.domain.auth.application.dto.OAuthLoginUserResponse;
import com.ject6.boost.domain.auth.application.dto.TokenRefreshResponse;
import com.ject6.boost.domain.auth.domain.OAuthProvider;
import com.ject6.boost.domain.auth.infrastructure.OAuthRedisKeys;
import com.ject6.boost.domain.auth.infrastructure.oauth.OAuthClientProperties;
import com.ject6.boost.domain.auth.presentaion.exception.AuthErrorCode;
import com.ject6.boost.domain.user.domain.entity.User;
import com.ject6.boost.domain.user.domain.entity.UserOAuthAccount;
import com.ject6.boost.domain.user.infrastructure.repository.UserOAuthAccountRepository;
import com.ject6.boost.domain.user.infrastructure.repository.UserRepository;
import java.time.Duration;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
public class AuthService {

    private static final String TOKEN_TYPE = "Bearer";

    private final OAuthClientProperties oauthClientProperties;
    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;
    private final JwtTokenProvider jwtTokenProvider;
    private final UserRepository userRepository;
    private final UserOAuthAccountRepository userOAuthAccountRepository;

    /**
     * OAuth provider 식별자로 서비스 사용자를 찾거나 생성하고 서비스 access token 응답을 만드는 함수.
     */
    @Transactional
    public OAuthLoginResult login(OAuthProvider provider, String providerUserId) {
        if (provider == null || !StringUtils.hasText(providerUserId)) {
            throw new BusinessException(AuthErrorCode.OAUTH_USER_ID_MISSING);
        }

        User user = findOrCreateUser(provider, providerUserId);
        AuthenticatedUser authenticatedUser = new AuthenticatedUser(user.getId(), List.of("USER"));
        JwtToken accessToken = issueAccessToken(authenticatedUser);
        JwtToken refreshToken = issueRefreshToken(authenticatedUser);

        OAuthLoginResponse response = new OAuthLoginResponse(
                accessToken.token(),
                TOKEN_TYPE,
                accessToken.expiresIn(),
                new OAuthLoginUserResponse(user.getId(), provider, user.isOnboardingCompleted())
        );

        return new OAuthLoginResult(response, refreshToken.token(), refreshToken.expiresIn());
    }

    /**
     * refresh token을 검증하고 새 서비스 access token을 발급하는 함수.
     */
    public TokenRefreshResponse refresh(String refreshToken) {
        if (!StringUtils.hasText(refreshToken)) {
            throw new BusinessException(AuthErrorCode.REFRESH_TOKEN_REQUIRED);
        }

        String refreshTokenId = jwtTokenProvider.validateRefreshTokenAndGetTokenId(refreshToken);
        String refreshSession = redisTemplate.opsForValue().get(OAuthRedisKeys.REFRESH_KEY_PREFIX + refreshTokenId);

        if (!StringUtils.hasText(refreshSession)) {
            throw new BusinessException(AuthErrorCode.REFRESH_SESSION_NOT_FOUND);
        }

        AuthenticatedUser authenticatedUser = readAuthenticatedUser(refreshSession);
        JwtToken accessToken = issueAccessToken(authenticatedUser);

        return new TokenRefreshResponse(
                accessToken.token(),
                TOKEN_TYPE,
                accessToken.expiresIn()
        );
    }

    /**
     * access token과 refresh token에 연결된 Redis 세션을 삭제하는 함수.
     */
    public void logout(String accessToken, String refreshToken) {
        if (StringUtils.hasText(accessToken)) {
            String accessTokenId = jwtTokenProvider.validateAndGetTokenId(accessToken);
            redisTemplate.delete(OAuthRedisKeys.SESSION_KEY_PREFIX + accessTokenId);
        }
        if (StringUtils.hasText(refreshToken)) {
            String refreshTokenId = jwtTokenProvider.validateRefreshTokenAndGetTokenId(refreshToken);
            redisTemplate.delete(OAuthRedisKeys.REFRESH_KEY_PREFIX + refreshTokenId);
        }
    }

    /**
     * OAuth 계정 연결 정보로 기존 사용자를 조회하고 없으면 새 사용자와 연결 계정을 생성하는 함수.
     */
    private User findOrCreateUser(OAuthProvider provider, String providerUserId) {
        Optional<UserOAuthAccount> account =
                userOAuthAccountRepository.findActiveByProviderAndProviderUserId(provider, providerUserId);

        if (account.isPresent()) {
            return account.get().getUser();
        }

        User user = userRepository.save(User.create());
        userOAuthAccountRepository.save(UserOAuthAccount.create(user, provider, providerUserId));
        return user;
    }

    /**
     * 인증된 사용자 정보로 JWT를 발급하고 Redis 세션을 저장하는 함수.
     */
    private JwtToken issueAccessToken(AuthenticatedUser authenticatedUser) {
        JwtToken accessToken = jwtTokenProvider.issue(authenticatedUser);
        String redisKey = OAuthRedisKeys.SESSION_KEY_PREFIX + accessToken.id();

        saveSession(redisKey, authenticatedUser, oauthClientProperties.getSessionTtl());
        return accessToken;
    }

    /**
     * 인증된 사용자 정보로 refresh token을 발급하고 Redis refresh 세션을 저장하는 함수.
     */
    private JwtToken issueRefreshToken(AuthenticatedUser authenticatedUser) {
        JwtToken refreshToken = jwtTokenProvider.issueRefreshToken(authenticatedUser);
        String redisKey = OAuthRedisKeys.REFRESH_KEY_PREFIX + refreshToken.id();

        saveSession(redisKey, authenticatedUser, oauthClientProperties.getRefreshSessionTtl());
        return refreshToken;
    }

    /**
     * 인증된 사용자 정보를 Redis 세션에 JSON으로 저장하는 함수.
     */
    private void saveSession(String redisKey, AuthenticatedUser authenticatedUser, Duration ttl) {
        try {
            redisTemplate.opsForValue().set(
                    redisKey,
                    objectMapper.writeValueAsString(authenticatedUser),
                    ttl
            );
        } catch (JsonProcessingException exception) {
            throw new BusinessException(AuthErrorCode.LOGIN_TOKEN_ISSUE_FAILED);
        }
    }

    /**
     * Redis에 저장된 인증 사용자 JSON을 객체로 복원하는 함수.
     */
    private AuthenticatedUser readAuthenticatedUser(String session) {
        try {
            return objectMapper.readValue(session, AuthenticatedUser.class);
        } catch (JsonProcessingException exception) {
            throw new BusinessException(AuthErrorCode.REFRESH_SESSION_NOT_FOUND);
        }
    }
}
