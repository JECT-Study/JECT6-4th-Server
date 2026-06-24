package com.ject6.boost.domain.blog.repository;

import com.ject6.boost.presentation.blog.dto.BloggerResponse;
import com.ject6.boost.presentation.blog.dto.RecommendedCampaignResponse;
import com.ject6.boost.presentation.onboarding.dto.OnboardingRecommendResponse;
import java.util.List;

public interface BlogRecommendationRepository {
    List<RecommendedCampaignResponse.CampaignItem> findRecommendedCampaigns(Long userId, Long analysisId, int limit);

    /** 온보딩 프로필 텍스트를 서버 사이드 임베딩 없이 Spring AI pgvector로 검색하는 대신,
     *  Analyzer가 미리 저장한 profile_embeddings를 쿼리 벡터로 사용한다.
     *  category, thumbnailUrl, applyEndDate가 포함된 온보딩 응답 DTO를 직접 반환한다. */
    List<OnboardingRecommendResponse.CampaignItem> findRecommendedCampaignsByProfileEmbedding(Long userId, int limit);

    BloggerCandidates findBloggerCandidates(Long userId, Long analysisId, int limit);

    record BloggerCandidates(
            String category,
            List<BloggerResponse.BloggerItem> bloggers
    ) {}
}
