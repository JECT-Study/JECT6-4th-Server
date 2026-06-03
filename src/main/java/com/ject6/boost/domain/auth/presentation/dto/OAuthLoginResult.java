package com.ject6.boost.domain.auth.presentation.dto;

public record OAuthLoginResult(
        OAuthLoginResponse response,
        String refreshToken,
        long refreshTokenExpiresIn
) {
}
