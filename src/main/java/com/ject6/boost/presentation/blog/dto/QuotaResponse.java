package com.ject6.boost.presentation.blog.dto;

import java.time.OffsetDateTime;

public record QuotaResponse(
        int used,
        int limit,
        int remaining,
        OffsetDateTime resetAt
) {}
