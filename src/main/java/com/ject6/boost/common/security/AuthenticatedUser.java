package com.ject6.boost.common.security;

import java.util.List;

public record AuthenticatedUser(
        Long userId,
        List<String> roles
) {
}
