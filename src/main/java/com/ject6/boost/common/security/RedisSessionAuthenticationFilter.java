package com.ject6.boost.common.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ject6.boost.common.exception.BusinessException;
import com.ject6.boost.common.exception.GlobalErrorCode;
import com.ject6.boost.domain.auth.infrastructure.OAuthRedisKeys;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;
import java.util.List;

@Component
@RequiredArgsConstructor
public class RedisSessionAuthenticationFilter extends OncePerRequestFilter {

    private static final String BEARER_PREFIX = "Bearer ";

    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;
    private final JwtTokenProvider jwtTokenProvider;

    /**
     * Authorization 헤더의 access token으로 Redis 세션을 확인하고 SecurityContext에 인증 정보를 저장하는 함수.
     */
    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        String authorization = request.getHeader(HttpHeaders.AUTHORIZATION);

        if (SecurityContextHolder.getContext().getAuthentication() == null
                && StringUtils.hasText(authorization)
                && authorization.startsWith(BEARER_PREFIX)) {
            authenticateWithRedisSession(authorization.substring(BEARER_PREFIX.length()));
        }

        filterChain.doFilter(request, response);
    }

    /**
     * access token을 검증하고 Redis에 저장된 인증 사용자 정보를 복원하는 함수.
     */
    private void authenticateWithRedisSession(String accessToken) throws IOException {
        String tokenId = jwtTokenProvider.validateAndGetTokenId(accessToken);
        String session = redisTemplate.opsForValue().get(OAuthRedisKeys.SESSION_KEY_PREFIX + tokenId);

        if (!StringUtils.hasText(session)) {
            throw new BusinessException(GlobalErrorCode.UNAUTHORIZED_REQUEST);
        }

        AuthenticatedUser user = objectMapper.readValue(session, AuthenticatedUser.class);
        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(user, accessToken, toAuthorities(user.roles()));

        SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    private List<SimpleGrantedAuthority> toAuthorities(List<String> roles) {
        return roles.stream()
                .map(role -> new SimpleGrantedAuthority("ROLE_" + role))
                .toList();
    }
}
