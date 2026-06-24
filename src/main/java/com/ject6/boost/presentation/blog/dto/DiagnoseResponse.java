package com.ject6.boost.presentation.blog.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;
import java.util.Map;

public record DiagnoseResponse(
        @JsonProperty("id")           Long id,
        @JsonProperty("user_id")      Long userId,
        @JsonProperty("metrics")      Map<String, Object> metrics,
        @JsonProperty("category_fit") List<Object> categoryFit,
        @JsonProperty("strengths")    List<String> strengths,
        @JsonProperty("weaknesses")   List<String> weaknesses,
        @JsonProperty("has_embedding") boolean hasEmbedding
) {}
