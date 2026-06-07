package com.ject6.boost.domain.blog.infrastructure.impl;

import com.ject6.boost.domain.blog.domain.repository.BlogRecommendationRepository;
import com.ject6.boost.domain.blog.presentation.dto.BloggerResponse;
import com.ject6.boost.domain.blog.presentation.dto.RecommendedCampaignResponse;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

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
                    COALESCE(ca.id, ranked.document_id) AS id,
                    COALESCE(ca.title, ranked.document_title) AS title,
                    ranked.score AS fitness_score,
                    ranked.score AS selection_score
                FROM ranked
                LEFT JOIN campaigns ca
                  ON ca.id = CASE
                      WHEN ranked.external_id ~ '^[0-9]+$' THEN ranked.external_id::bigint
                      ELSE NULL
                  END
                WHERE ca.deleted_at IS NULL OR ca.id IS NULL
                ORDER BY ranked.score DESC
                LIMIT ?
                """;

        return jdbcTemplate.query(sql, (rs, rowNum) -> {
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

        List<BloggerResponse.BloggerItem> bloggers = jdbcTemplate.query(sql, (rs, rowNum) ->
                new BloggerResponse.BloggerItem(
                        rs.getString("nickname"),
                        toScore(rs.getDouble("overall_score")),
                        rs.getString("profile_url")
                ), userId, analysisId, analysisId, limit);

        String category = findAnalysisCategory(userId, analysisId);
        return new BloggerCandidates(category, bloggers);
    }

    private String findAnalysisCategory(Long userId, Long analysisId) {
        String sql = """
                WITH
                """ + SOURCE_DOC_CTE + """
                SELECT COALESCE(
                    aj.result #>> '{top_categories,0,category}',
                    aj.result #>> '{key_topics,0}',
                    'ALL'
                ) AS category
                FROM analysis_jobs aj
                JOIN source_doc sd ON sd.id = aj.document_id
                ORDER BY aj.created_at DESC
                LIMIT 1
                """;

        List<String> categories = jdbcTemplate.query(
                sql,
                (rs, rowNum) -> rs.getString("category"),
                userId,
                analysisId,
                analysisId
        );
        if (categories.isEmpty() || categories.get(0) == null || categories.get(0).isBlank()) {
            return "ALL";
        }
        return categories.get(0);
    }

    private int toScore(double similarity) {
        double normalized = Math.max(0.0, Math.min(1.0, similarity));
        return (int) Math.round(normalized * 100);
    }
}
