package com.ject6.boost.domain.auth.presentation.dto;

public record OAuthLoginResponse(
        String accessToken,
        String tokenType,
        long expiresIn,
        OAuthLoginUserResponse user
) {
}
