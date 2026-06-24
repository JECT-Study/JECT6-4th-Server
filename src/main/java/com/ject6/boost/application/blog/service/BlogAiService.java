package com.ject6.boost.application.blog.service;

import com.ject6.boost.application.blog.exception.BlogErrorCode;
import com.ject6.boost.application.common.exception.BusinessException;
import com.ject6.boost.infrastructure.common.queue.AnalysisQueuePublisher;
import com.ject6.boost.infrastructure.common.redis.AnalysisCacheService;
import com.ject6.boost.domain.blog.repository.BlogRecommendationRepository;
import com.ject6.boost.domain.campaign.entity.Campaign;
import com.ject6.boost.domain.campaign.repository.CampaignRepository;
import com.ject6.boost.infrastructure.blog.client.CrawlerClient;
import com.ject6.boost.infrastructure.blog.client.PythonAiClient;
import com.ject6.boost.infrastructure.blog.client.dto.ConversationRequest;
import com.ject6.boost.infrastructure.blog.client.dto.ConversationResponse;
import com.ject6.boost.presentation.blog.dto.*;
import com.ject6.boost.presentation.blog.dto.DiagnoseRequest;
import com.ject6.boost.presentation.blog.dto.DiagnoseResponse;
import com.ject6.boost.domain.user.entity.BlogAnalysisResult;
import com.ject6.boost.domain.user.entity.UserBlog;
import com.ject6.boost.domain.user.repository.BlogAnalysisResultRepository;
import com.ject6.boost.domain.user.repository.UserBlogRepository;
import com.ject6.boost.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.IntStream;

@Slf4j
@Service
@RequiredArgsConstructor
public class BlogAiService {

    private static final int FREE_PLAN_VISIBLE_COUNT = 3;
    private static final int RECOMMENDATION_LIMIT = 8;
    private static final Set<String> VALID_MODES = Set.of("FULL_BLOG", "POST");

    private final AnalysisQueuePublisher queuePublisher;
    private final PythonAiClient pythonAiClient;
    private final CrawlerClient crawlerClient;
    private final BlogCrawlerAsyncTrigger crawlerAsyncTrigger;
    private final BlogAnalysisResultRepository blogAnalysisResultRepository;
    private final BlogRecommendationRepository blogRecommendationRepository;
    private final CampaignRepository campaignRepository;
    private final UserRepository userRepository;
    private final UserBlogRepository userBlogRepository;
    private final AnalysisCacheService analysisCacheService;
    private final DiagnosisQuotaService diagnosisQuotaService;

    /**
     * POST /blog/analyze
     * 연결된 블로그를 검증하고, 분석 모드에 따라 크롤러를 트리거하거나 분석 큐에 발행합니다.
     *
     * FULL_BLOG: 전체 포스트를 크롤링·집계 후 BLOG_SNAPSHOT 1건 분석 (기본값)
     * POST:      개별 포스트 분석 — documentId 있으면 즉시 큐 발행, 없으면 크롤링 후 포스트별 분석
     */
    @Transactional
    public AnalyzeResponse requestAnalysis(Long userId, AnalyzeRequest request) {
        UserBlog blog = userRepository.findActiveById(userId)
                .flatMap(user -> userBlogRepository.findActiveByUser(user).stream().findFirst())
                .orElseThrow(() -> new BusinessException(BlogErrorCode.BLOG_NOT_CONNECTED));

        String correlationId = UUID.randomUUID().toString();
        String rawMode = request.analysisMode();
        String mode = (rawMode != null) ? rawMode.trim().toUpperCase() : "FULL_BLOG";
        if (!VALID_MODES.contains(mode)) {
            throw new BusinessException(BlogErrorCode.INVALID_ANALYSIS_MODE);
        }

        boolean forceRefresh = Boolean.TRUE.equals(request.forceRefresh());

        if ("FULL_BLOG".equals(mode)) {
            // 캐시 hit 확인 (forceRefresh가 아닌 경우)
            if (!forceRefresh) {
                Optional<String> cached = analysisCacheService.getFullBlogCache(userId, blog.getId());
                if (cached.isPresent()) {
                    log.info("FULL_BLOG 캐시 hit userId={} jobId={}", userId, cached.get());
                    return new AnalyzeResponse(null, "cached",
                            "이미 분석된 결과가 있습니다. 최신 분석을 원하시면 forceRefresh=true를 사용하세요.",
                            null, null, null, true, Long.parseLong(cached.get()));
                }
            }

            boolean lockAcquired = false;
            boolean quotaReserved = false;
            try {
                // 의미 기반 lock: 동일 (userId, blogId) 쌍의 중복 요청을 차단
                if (!analysisCacheService.acquireFullBlogLock(userId, blog.getId())) {
                    log.warn("FULL_BLOG 중복 요청 감지 userId={} blogId={}", userId, blog.getId());
                    return new AnalyzeResponse(null, "in_progress",
                            "이미 분석이 진행 중입니다.", null, correlationId, null, false, null);
                }
                lockAcquired = true;

                // correlationId → {userId, blogId} 매핑을 Redis에 저장 (완료 이벤트 수신 시 lock 해제 + 캐시 키 산출)
                analysisCacheService.storeCorrelationContext(correlationId, userId, blog.getId());

                // FULL_BLOG 진단 쿼터 예약
                diagnosisQuotaService.reserveOrThrow(userId);
                quotaReserved = true;

                String batchId = UUID.randomUUID().toString();
                crawlerAsyncTrigger.triggerAsync(blog.getBlogUrl(), userId, blog.getId(), correlationId, "FULL_BLOG", batchId);
                log.info("FULL_BLOG 분석 요청 userId={} correlationId={} batchId={}", userId, correlationId, batchId);
                return new AnalyzeResponse(null, "crawling",
                        "블로그 전체 분석을 준비 중입니다. 크롤링 및 집계 완료 후 분석이 시작됩니다.",
                        null, correlationId, batchId, false, null);
            } catch (RuntimeException e) {
                if (quotaReserved) {
                    diagnosisQuotaService.releaseReservation(userId);
                }
                if (lockAcquired) {
                    analysisCacheService.releaseFullBlogLock(userId, blog.getId());
                    analysisCacheService.deleteCorrelationContext(correlationId);
                }
                throw e;
            }
        }

        // POST mode
        Long docId = request.documentId();
        if (docId != null) {
            // POST 캐시 hit 확인
            if (!forceRefresh) {
                Optional<String> cached = analysisCacheService.getPostCache(userId, docId);
                if (cached.isPresent()) {
                    log.info("POST 캐시 hit userId={} documentId={} jobId={}", userId, docId, cached.get());
                    return new AnalyzeResponse(docId, "cached",
                            "이미 분석된 결과가 있습니다.", null, null, null, true, Long.parseLong(cached.get()));
                }
            }
            queuePublisher.publishWithMode(userId, docId, correlationId, "POST");
            try {
                blogAnalysisResultRepository.save(BlogAnalysisResult.create(
                        userRepository.findActiveById(userId).orElseThrow(), blog, docId));
            } catch (Exception e) {
                log.warn("blog_analysis_results 저장 실패 (non-critical) userId={}: {}", userId, e.getMessage());
            }
            return new AnalyzeResponse(docId, "pending", "분석이 요청되었습니다.", null, correlationId, null, false, null);
        }

        // POST + documentId 없음 → 크롤링 후 IngestWorker가 포스트별 분석 큐 발행
        crawlerAsyncTrigger.triggerAsync(blog.getBlogUrl(), userId, blog.getId(), correlationId, "POST", null);
        log.info("POST 분석 요청 (documentId 없음) userId={} correlationId={}", userId, correlationId);
        return new AnalyzeResponse(null, "crawling", "블로그 크롤링 중입니다. 잠시 후 분석이 시작됩니다.",
                null, correlationId, null, false, null);
    }

    @Transactional(readOnly = true)
    public QuotaResponse getQuota(Long userId) {
        return diagnosisQuotaService.getQuota(userId);
    }

    /**
     * GET /blog/analysis/{documentId}
     * HTTP — Python analysis_jobs 테이블에서 조회
     */
    public BlogAnalysisDetailResponse getAnalysis(Long documentId) {
        return BlogAnalysisDetailResponse.from(pythonAiClient.getAnalysis(documentId));
    }

    /**
     * GET /blog/analysis/{id}/recommendations
     * pgvector 유사도 검색 → 결과 없으면 active 캠페인 fallback
     */
    public RecommendedCampaignResponse getRecommendations(Long userId, Long analysisId) {
        List<RecommendedCampaignResponse.CampaignItem> campaigns;
        try {
            campaigns = blogRecommendationRepository.findRecommendedCampaigns(userId, analysisId, RECOMMENDATION_LIMIT);
        } catch (Exception e) {
            log.warn("pgvector 추천 쿼리 실패, fallback 사용 userId={} analysisId={}: {}", userId, analysisId, e.getMessage());
            campaigns = List.of();
        }

        if (campaigns.isEmpty()) {
            log.info("추천 결과 없음 — fallback 사용 userId={} analysisId={}", userId, analysisId);
            List<Campaign> fallback = campaignRepository.findActiveFallback(RECOMMENDATION_LIMIT);
            campaigns = fallback.stream()
                    .map(c -> new RecommendedCampaignResponse.CampaignItem(
                            c.getId(), c.getTitle(), 70, 70,
                            "FALLBACK", "AI 분석 기반 추천 공고입니다."
                    ))
                    .toList();
        }
        return new RecommendedCampaignResponse(analysisId, campaigns);
    }

    /**
     * GET /blog/analysis/{id}/bloggers
     * 카테고리별 인기 블로거 Top3 (추후 구현)
     */
    public BloggerResponse getBloggers(Long userId, Long analysisId) {
        BlogRecommendationRepository.BloggerCandidates candidates =
                blogRecommendationRepository.findBloggerCandidates(userId, analysisId, 3);
        return new BloggerResponse(candidates.category(), candidates.bloggers());
    }

    /**
     * GET /blog/analysis/history
     * Spring 자체 blog_analysis_results 테이블 조회
     */
    @Transactional(readOnly = true)
    public BlogAnalysisHistoryResponse getHistory(Long userId, boolean isPremium) {
        List<BlogAnalysisResult> results = blogAnalysisResultRepository.findByUserIdAndDeletedAtIsNull(userId);
        int visibleCount = isPremium ? results.size() : Math.min(results.size(), FREE_PLAN_VISIBLE_COUNT);

        List<BlogAnalysisHistoryResponse.HistoryItem> items = IntStream.range(0, results.size())
                .mapToObj(i -> {
                    BlogAnalysisResult r = results.get(i);
                    boolean locked = !isPremium && i >= FREE_PLAN_VISIBLE_COUNT;
                    String channelUrl = r.getBlog() != null ? r.getBlog().getBlogUrl() : null;
                    return new BlogAnalysisHistoryResponse.HistoryItem(r.getId(), channelUrl, r.getCreatedAt(), locked);
                })
                .toList();

        return new BlogAnalysisHistoryResponse(items, results.size(), visibleCount);
    }

    /**
     * POST /blog/chat
     * HTTP 동기 프록시 → Python POST /v1/conversations/messages
     */
    public ChatResponse chat(Long userId, ChatRequest request) {
        ConversationResponse resp = pythonAiClient.sendChat(
                new ConversationRequest(userId, request.sessionId(), request.documentId(), request.message())
        );
        return new ChatResponse(resp.sessionId(), resp.reply(), resp.tokensUsed(), resp.tokensRemaining());
    }

    /**
     * POST /blog/diagnosis
     * C-1: R2 6지표 진단 — Analyzer POST /v1/diagnosis 프록시
     */
    public DiagnoseResponse runDiagnosis(Long userId, Long documentId) {
        return pythonAiClient.runDiagnosis(userId, documentId);
    }

    /**
     * DELETE /blog/chat/{sessionId}
     * HTTP 동기 프록시 → Python DELETE /v1/conversations/{sessionId}
     */
    public void resetSession(String sessionId) {
        pythonAiClient.resetSession(sessionId);
    }
}
