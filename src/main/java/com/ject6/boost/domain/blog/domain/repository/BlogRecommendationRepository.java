package com.ject6.boost.domain.blog.domain.repository;

import com.ject6.boost.domain.blog.presentation.dto.BloggerResponse;
import com.ject6.boost.domain.blog.presentation.dto.RecommendedCampaignResponse;
import java.util.List;

public interface BlogRecommendationRepository {
    List<RecommendedCampaignResponse.CampaignItem> findRecommendedCampaigns(Long userId, Long analysisId, int limit);

    BloggerCandidates findBloggerCandidates(Long userId, Long analysisId, int limit);

    record BloggerCandidates(
            String category,
            List<BloggerResponse.BloggerItem> bloggers
    ) {}
}
