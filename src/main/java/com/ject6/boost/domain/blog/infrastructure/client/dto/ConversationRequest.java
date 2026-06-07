package com.ject6.boost.domain.blog.infrastructure.client.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record ConversationRequest(
        @JsonProperty("user_id")     Long userId,
        @JsonProperty("session_id")  String sessionId,
        @JsonProperty("document_id") Long documentId,
        @JsonProperty("message")     String message
) {}
