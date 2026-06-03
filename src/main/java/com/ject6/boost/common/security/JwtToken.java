package com.ject6.boost.common.security;

public record JwtToken(
        String token,
        String id,
        long expiresIn
) {
}
