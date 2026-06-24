package com.ject6.boost.infrastructure.common.queue;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Analyzer → Spring: 분석 완료 이벤트 페이로드.
 * analysisMode가 "DIAGNOSIS"인 경우만 DiagnosisQuota 차감 대상이 된다.
 */
public record AnalysisCompletedMessage(
        @JsonProperty("user_id")         Long userId,
        @JsonProperty("document_id")     Long documentId,
        @JsonProperty("analysis_job_id") Long analysisJobId,
        @JsonProperty("analysis_mode")   String analysisMode,
        @JsonProperty("blog_id")         Long blogId,
        @JsonProperty("batch_id")        String batchId,
        @JsonProperty("correlation_id")  String correlationId,
        @JsonProperty("status")          String status
) {}
