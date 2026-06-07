package com.ject6.boost.domain.blog.presentation.dto;

public record ChatResponse(
        String sessionId,
        String reply,
        int tokensUsed,
        int tokensRemaining
) {}
