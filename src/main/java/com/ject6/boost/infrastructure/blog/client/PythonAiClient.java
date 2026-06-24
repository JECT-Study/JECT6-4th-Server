package com.ject6.boost.infrastructure.blog.client;

import com.ject6.boost.application.common.exception.BusinessException;
import com.ject6.boost.application.blog.exception.BlogErrorCode;
import com.ject6.boost.infrastructure.blog.client.dto.AnalysisResultResponse;
import com.ject6.boost.infrastructure.blog.client.dto.ConversationRequest;
import com.ject6.boost.infrastructure.blog.client.dto.ConversationResponse;
import com.ject6.boost.infrastructure.blog.client.dto.ProfileEmbeddingRequest;
import com.ject6.boost.infrastructure.blog.client.dto.ProfileEmbeddingResponse;
import com.ject6.boost.presentation.blog.dto.DiagnoseResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;

@Slf4j
@Component
public class PythonAiClient {

    private final RestClient restClient;

    public PythonAiClient(@Value("${python.ai.server.url:http://localhost:8000}") String baseUrl) {
        this.restClient = RestClient.builder()
                .baseUrl(baseUrl)
                .defaultHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                .build();
    }

    public AnalysisResultResponse getAnalysis(Long documentId) {
        try {
            return restClient.get()
                    .uri("/v1/analysis/documents/{id}", documentId)
                    .retrieve()
                    .body(AnalysisResultResponse.class);
        } catch (HttpClientErrorException.NotFound e) {
            throw new BusinessException(BlogErrorCode.ANALYSIS_NOT_FOUND);
        } catch (Exception e) {
            log.error("Python AI server error on getAnalysis({}): {}", documentId, e.getMessage());
            throw new BusinessException(BlogErrorCode.ANALYZE_SERVER_ERROR);
        }
    }

    public ConversationResponse sendChat(ConversationRequest request) {
        try {
            return restClient.post()
                    .uri("/v1/conversations/messages")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(request)
                    .retrieve()
                    .body(ConversationResponse.class);
        } catch (HttpClientErrorException.BadRequest e) {
            throw new BusinessException(BlogErrorCode.CHAT_TOKEN_LIMIT_EXCEEDED);
        } catch (HttpClientErrorException.TooManyRequests e) {
            throw new BusinessException(BlogErrorCode.CHAT_RATE_LIMIT_EXCEEDED);
        } catch (Exception e) {
            log.error("Python AI server error on sendChat: {}", e.getMessage());
            throw new BusinessException(BlogErrorCode.ANALYZE_SERVER_ERROR);
        }
    }

    public void resetSession(String sessionId) {
        try {
            restClient.delete()
                    .uri("/v1/conversations/{sessionId}", sessionId)
                    .retrieve()
                    .toBodilessEntity();
        } catch (Exception e) {
            log.warn("Failed to reset session {}: {}", sessionId, e.getMessage());
        }
    }

    /** C-1: R2 6지표 진단 — Analyzer POST /v1/diagnosis 프록시 */
    public DiagnoseResponse runDiagnosis(Long userId, Long documentId) {
        try {
            var body = java.util.Map.of("user_id", userId, "document_id", documentId);
            return restClient.post()
                    .uri("/v1/diagnosis")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(body)
                    .retrieve()
                    .body(DiagnoseResponse.class);
        } catch (HttpClientErrorException.NotFound e) {
            throw new BusinessException(BlogErrorCode.ANALYSIS_NOT_FOUND);
        } catch (Exception e) {
            log.error("Python AI server error on runDiagnosis userId={} documentId={}: {}", userId, documentId, e.getMessage());
            throw new BusinessException(BlogErrorCode.ANALYZE_SERVER_ERROR);
        }
    }

    public ProfileEmbeddingResponse embedOnboardingProfile(ProfileEmbeddingRequest request) {
        try {
            return restClient.post()
                    .uri("/v1/profile/embed")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(request)
                    .retrieve()
                    .body(ProfileEmbeddingResponse.class);
        } catch (Exception e) {
            log.warn("Failed to embed onboarding profile userId={}: {}", request.userId(), e.getMessage());
            return null;
        }
    }
}
