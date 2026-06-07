package com.ject6.boost.domain.blog.presentation.dto;

import java.util.List;

public record RecommendedCampaignResponse(
        Long analysisId,
        List<CampaignItem> campaigns
) {
    public record CampaignItem(
            Long id,
            String title,
            int fitnessScore,
            int selectionScore,
            String reasonType,
            String reasonMessage
    ) {}
}
