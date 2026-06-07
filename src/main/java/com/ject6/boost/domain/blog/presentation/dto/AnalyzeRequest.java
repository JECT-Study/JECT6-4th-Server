package com.ject6.boost.domain.blog.presentation.dto;

import jakarta.validation.constraints.NotNull;

public record AnalyzeRequest(
        @NotNull Long blogId,
        @NotNull Long documentId
) {}
