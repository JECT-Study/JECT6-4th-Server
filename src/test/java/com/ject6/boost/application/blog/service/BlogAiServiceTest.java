package com.ject6.boost.application.blog.service;

import com.ject6.boost.domain.blog.repository.BlogRecommendationRepository;
import com.ject6.boost.domain.campaign.repository.CampaignRepository;
import com.ject6.boost.domain.user.entity.User;
import com.ject6.boost.domain.user.entity.UserBlog;
import com.ject6.boost.domain.user.repository.BlogAnalysisResultRepository;
import com.ject6.boost.domain.user.repository.UserBlogRepository;
import com.ject6.boost.domain.user.repository.UserRepository;
import com.ject6.boost.infrastructure.blog.client.CrawlerClient;
import com.ject6.boost.infrastructure.blog.client.PythonAiClient;
import com.ject6.boost.infrastructure.common.queue.AnalysisQueuePublisher;
import com.ject6.boost.infrastructure.common.redis.AnalysisCacheService;
import com.ject6.boost.presentation.blog.dto.AnalyzeRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willThrow;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BlogAiServiceTest {

    @Mock private AnalysisQueuePublisher queuePublisher;
    @Mock private PythonAiClient pythonAiClient;
    @Mock private CrawlerClient crawlerClient;
    @Mock private BlogCrawlerAsyncTrigger crawlerAsyncTrigger;
    @Mock private BlogAnalysisResultRepository blogAnalysisResultRepository;
    @Mock private BlogRecommendationRepository blogRecommendationRepository;
    @Mock private CampaignRepository campaignRepository;
    @Mock private UserRepository userRepository;
    @Mock private UserBlogRepository userBlogRepository;
    @Mock private AnalysisCacheService analysisCacheService;
    @Mock private DiagnosisQuotaService diagnosisQuotaService;

    private BlogAiService sut;
    private User user;
    private UserBlog blog;

    @BeforeEach
    void setUp() {
        sut = new BlogAiService(
                queuePublisher,
                pythonAiClient,
                crawlerClient,
                crawlerAsyncTrigger,
                blogAnalysisResultRepository,
                blogRecommendationRepository,
                campaignRepository,
                userRepository,
                userBlogRepository,
                analysisCacheService,
                diagnosisQuotaService
        );

        user = mock(User.class);
        blog = mock(UserBlog.class);
        given(blog.getId()).willReturn(20L);
        given(userRepository.findActiveById(1L)).willReturn(Optional.of(user));
        given(userBlogRepository.findActiveByUser(user)).willReturn(List.of(blog));
        given(analysisCacheService.getFullBlogCache(1L, 20L)).willReturn(Optional.empty());
        given(analysisCacheService.acquireFullBlogLock(1L, 20L)).willReturn(true);
    }

    @Test
    @DisplayName("FULL_BLOG 예약 실패 시 lock/context를 보상 해제한다")
    void requestAnalysis_releasesLockAndContextWhenQuotaReservationFails() {
        RuntimeException failure = new RuntimeException("quota failure");
        willThrow(failure).given(diagnosisQuotaService).reserveOrThrow(1L);

        assertThatThrownBy(() -> sut.requestAnalysis(1L, fullBlogRequest()))
                .isSameAs(failure);

        verify(analysisCacheService).releaseFullBlogLock(1L, 20L);
        verify(analysisCacheService).deleteCorrelationContext(anyString());
        verify(diagnosisQuotaService, never()).releaseReservation(1L);
        verify(crawlerAsyncTrigger, never()).triggerAsync(anyString(), eq(1L), eq(20L), anyString(), eq("FULL_BLOG"), anyString());
    }

    @Test
    @DisplayName("FULL_BLOG 크롤러 트리거 실패 시 예약과 lock/context를 함께 보상 해제한다")
    void requestAnalysis_releasesQuotaLockAndContextWhenCrawlerTriggerFails() {
        given(blog.getBlogUrl()).willReturn("https://example.com/blog");
        RuntimeException failure = new RuntimeException("crawler failure");
        willThrow(failure).given(crawlerAsyncTrigger)
                .triggerAsync(eq("https://example.com/blog"), eq(1L), eq(20L), anyString(), eq("FULL_BLOG"), anyString());

        assertThatThrownBy(() -> sut.requestAnalysis(1L, fullBlogRequest()))
                .isSameAs(failure);

        verify(diagnosisQuotaService).releaseReservation(1L);
        verify(analysisCacheService).releaseFullBlogLock(1L, 20L);
        verify(analysisCacheService).deleteCorrelationContext(anyString());
    }

    private AnalyzeRequest fullBlogRequest() {
        return new AnalyzeRequest(null, null, "FULL_BLOG", false);
    }
}
