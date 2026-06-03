package com.ject6.boost.domain.auth.application.dto;

public record TokenRefreshResponse(
        String accessToken,
        String tokenType,
        long expiresIn
) {
}
