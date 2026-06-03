package com.ject6.boost.domain.auth.presentation.dto;

public record TokenRefreshResponse(
        String accessToken,
        String tokenType,
        long expiresIn
) {
}
