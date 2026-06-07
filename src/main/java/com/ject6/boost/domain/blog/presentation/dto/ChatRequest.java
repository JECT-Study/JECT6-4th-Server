package com.ject6.boost.domain.blog.presentation.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record ChatRequest(
        @NotBlank String sessionId,
        @NotNull Long documentId,
        @NotBlank String message
) {}
