package com.ject6.boost.domain.my.presentation.dto;

import com.ject6.boost.domain.user.domain.entity.BlogAnalysisResult;
import java.time.OffsetDateTime;

public record AnalysisHistoryItemResponse(
        Long id,
        String channelUrl,
        OffsetDateTime analyzedAt,
        boolean isLocked
) {
    public static AnalysisHistoryItemResponse from(BlogAnalysisResult r, boolean locked) {
        return new AnalysisHistoryItemResponse(
                r.getId(),
                r.getActivityChannel() != null ? r.getActivityChannel().getUrl() : null,
                r.getCreatedAt(),
                locked
        );
    }
}
