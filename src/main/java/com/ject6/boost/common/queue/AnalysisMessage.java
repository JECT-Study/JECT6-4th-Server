package com.ject6.boost.common.queue;

import com.fasterxml.jackson.annotation.JsonProperty;

public record AnalysisMessage(
        @JsonProperty("user_id")     Long userId,
        @JsonProperty("document_id") Long documentId
) {}
