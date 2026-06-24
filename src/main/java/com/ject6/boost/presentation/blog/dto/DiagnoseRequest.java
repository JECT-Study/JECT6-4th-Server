package com.ject6.boost.presentation.blog.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record DiagnoseRequest(
        @NotNull @Positive Long documentId
) {}
