package com.ject6.boost.domain.blog.presentation.dto;

public record AnalyzeResponse(
        Long documentId,
        String status,
        String message,
        Integer aiCreditRemaining
) {}
