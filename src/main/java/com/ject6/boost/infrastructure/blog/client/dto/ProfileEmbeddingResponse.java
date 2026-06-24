package com.ject6.boost.infrastructure.blog.client.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record ProfileEmbeddingResponse(
        @JsonProperty("user_id")       Long userId,
        @JsonProperty("stored")        boolean stored,
        @JsonProperty("has_embedding") boolean hasEmbedding
) {}
