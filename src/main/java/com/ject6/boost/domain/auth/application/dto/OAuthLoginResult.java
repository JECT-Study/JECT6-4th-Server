package com.ject6.boost.domain.auth.application.dto;

public record OAuthLoginResult(
        OAuthLoginResponse response,
        String refreshToken,
        long refreshTokenExpiresIn
) {
}
