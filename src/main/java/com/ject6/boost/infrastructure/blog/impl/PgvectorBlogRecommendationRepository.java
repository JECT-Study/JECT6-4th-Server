package com.ject6.boost.infrastructure.blog.impl;

import com.ject6.boost.domain.blog.repository.BlogRecommendationRepository;
import com.ject6.boost.presentation.blog.dto.BloggerResponse;
import com.ject6.boost.presentation.blog.dto.RecommendedCampaignResponse;
import com.ject6.boost.presentation.onboarding.dto.OnboardingRecommendResponse;
import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Slf4j
@Repository
@RequiredArgsConstructor
public class PgvectorBlogRecommendationRepository implements BlogRecommendationRepository {

    private static final String SOURCE_DOC_CTE = """
            source_doc AS (
                SELECT d.id
                FROM documents d
                LEFT JOIN analysis_jobs aj ON aj.document_id = d.id
                WHERE d.user_id = ?
                  AND (d.id = ? OR aj.id = ?)
                ORDER BY aj.created_at DESC NULLS LAST
                LIMIT 1
            ),
            query_embedding AS (
                SELECT AVG(c.embedding) AS embedding
                FROM document_chunks c
                JOIN source_doc sd ON sd.id = c.document_id
            )
            """;

    private final JdbcTemplate jdbcTemplate;

    @Override
    public List<RecommendedCampaignResponse.CampaignItem> findRecommendedCampaigns(
            Long userId,
            Long analysisId,
            int limit
    ) {
        String sql = """
                WITH
                """ + SOURCE_DOC_CTE + """
                , ranked AS (
                    SELECT DISTINCT ON (d.id)
                        d.id AS document_id,
                        d.external_id,
                        d.title AS document_title,
                        1 - (c.embedding <=> qe.embedding) AS score
                    FROM document_chunks c
                    JOIN documents d ON d.id = c.document_id
                    CROSS JOIN query_embedding qe
                    WHERE d.source_type = 'job_posting'
                      AND qe.embedding IS NOT NULL
                    ORDER BY d.id, c.embedding <=> qe.embedding
                )
                SELECT
                    ca.id AS id,
                    ca.title AS title,
                    ranked.score AS fitness_score,
                    ranked.score AS selection_score
                FROM ranked
                JOIN campaigns ca ON ca.source_url = ranked.external_id
                WHERE ca.deleted_at IS NULL
                ORDER BY ranked.score DESC
                LIMIT ?
                """;

        List<RecommendedCampaignResponse.CampaignItem> items = jdbcTemplate.query(sql, (rs, rowNum) -> {
            int fitnessScore = toScore(rs.getDouble("fitness_score"));
            int selectionScore = toScore(rs.getDouble("selection_score"));
            return new RecommendedCampaignResponse.CampaignItem(
                    rs.getLong("id"),
                    rs.getString("title"),
                    fitnessScore,
                    selectionScore,
                    "BLOG_FITNESS",
                    "블로그 분석 결과와 유사도가 높은 체험단 공고입니다."
            );
        }, userId, analysisId, analysisId, limit);

        if (items.isEmpty()) {
            log.info("블로그 분석 추천 결과 없음 — userId={}, analysisId={}", userId, analysisId);
        }
        return items;
    }

    @Override
    public BloggerCandidates findBloggerCandidates(Long userId, Long analysisId, int limit) {
        String sql = """
                WITH
                """ + SOURCE_DOC_CTE + """
                , analysis_category AS (
                    SELECT COALESCE(
                        aj.result #>> '{top_categories,0,category}',
                        aj.result #>> '{key_topics,0}',
                        'ALL'
                    ) AS category
                    FROM analysis_jobs aj
                    JOIN source_doc sd ON sd.id = aj.document_id
                    ORDER BY aj.created_at DESC
                    LIMIT 1
                ),
                ranked AS (
                    SELECT DISTINCT ON (d.id)
                        d.title,
                        d.url,
                        d.doc_metadata,
                        1 - (c.embedding <=> qe.embedding) AS score
                    FROM document_chunks c
                    JOIN documents d ON d.id = c.document_id
                    CROSS JOIN query_embedding qe
                    WHERE d.source_type = 'ext_blog'
                      AND qe.embedding IS NOT NULL
                    ORDER BY d.id, c.embedding <=> qe.embedding
                )
                SELECT
                    COALESCE((SELECT category FROM analysis_category), 'ALL') AS category,
                    COALESCE(ranked.doc_metadata ->> 'nickname', ranked.title) AS nickname,
                    ranked.url AS profile_url,
                    ranked.score AS overall_score
                FROM ranked
                ORDER BY ranked.score DESC
                LIMIT ?
                """;

        String[] categoryHolder = {"ALL"};
        List<BloggerResponse.BloggerItem> bloggers = jdbcTemplate.query(sql, (rs, rowNum) -> {
            if (rowNum == 0) {
                String cat = rs.getString("category");
                if (cat != null && !cat.isBlank()) {
                    categoryHolder[0] = cat;
                }
            }
            return new BloggerResponse.BloggerItem(
                    rs.getString("nickname"),
                    toScore(rs.getDouble("overall_score")),
                    rs.getString("profile_url")
            );
        }, userId, analysisId, analysisId, limit);

        return new BloggerCandidates(categoryHolder[0], bloggers);
    }

    /**
     * R4: 온보딩 프로필 임베딩으로 공고 추천.
     * profile_embeddings 테이블에 Analyzer가 저장한 벡터를 쿼리 벡터로 사용한다.
     * 해당 레코드가 없으면 빈 리스트를 반환하고, 호출부에서 fallback 처리한다.
     */
    @Override
    public List<OnboardingRecommendResponse.CampaignItem> findRecommendedCampaignsByProfileEmbedding(
            Long userId,
            int limit
    ) {
        String sql = """
                WITH query_embedding AS (
                    SELECT embedding
                    FROM profile_embeddings
                    WHERE user_id = ?
                    ORDER BY created_at DESC
                    LIMIT 1
                ),
                ranked AS (
                    SELECT DISTINCT ON (d.id)
                        d.id AS document_id,
                        d.external_id,
                        1 - (c.embedding <=> qe.embedding) AS score
                    FROM document_chunks c
                    JOIN documents d ON d.id = c.document_id
                    CROSS JOIN query_embedding qe
                    WHERE d.source_type = 'job_posting'
                    ORDER BY d.id, c.embedding <=> qe.embedding
                )
                SELECT
                    ca.id              AS id,
                    ca.title           AS title,
                    ca.category        AS category,
                    ca.thumbnail_url   AS thumbnail_url,
                    ca.apply_end_date  AS apply_end_date
                FROM ranked
                JOIN campaigns ca ON ca.source_url = ranked.external_id
                WHERE ca.deleted_at IS NULL
                ORDER BY ranked.score DESC
                LIMIT ?
                """;
        try {
            return jdbcTemplate.query(sql, (rs, rowNum) -> new OnboardingRecommendResponse.CampaignItem(
                    rs.getLong("id"),
                    rs.getString("title"),
                    rs.getString("category"),
                    rs.getString("thumbnail_url"),
                    rs.getObject("apply_end_date", LocalDate.class)
            ), userId, limit);
        } catch (Exception e) {
            log.warn("onboarding profile embedding search failed userId={} err={}", userId, e.getMessage());
            return List.of();
        }
    }

    private int toScore(double similarity) {
        double normalized = Math.max(0.0, Math.min(1.0, similarity));
        return (int) Math.round(normalized * 100);
    }
}
