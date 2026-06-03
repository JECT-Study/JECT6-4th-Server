package com.ject6.boost.domain.auth.application.dto;

import com.ject6.boost.domain.auth.domain.OAuthProvider;

public record OAuthLoginUserResponse(
        Long id,
        OAuthProvider provider,
        boolean onboardingCompleted
) {
}
