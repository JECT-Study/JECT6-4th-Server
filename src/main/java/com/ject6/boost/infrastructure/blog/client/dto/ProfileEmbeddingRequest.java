package com.ject6.boost.infrastructure.blog.client.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record ProfileEmbeddingRequest(
        @JsonProperty("user_id")      Long userId,
        @JsonProperty("profile_text") String profileText,
        @JsonProperty("source")       String source
) {}
