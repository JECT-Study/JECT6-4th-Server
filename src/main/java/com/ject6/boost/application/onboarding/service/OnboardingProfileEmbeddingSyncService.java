package com.ject6.boost.application.onboarding.service;

import com.ject6.boost.domain.onboarding.entity.OnboardingResponse;
import com.ject6.boost.infrastructure.blog.client.PythonAiClient;
import com.ject6.boost.infrastructure.blog.client.dto.ProfileEmbeddingRequest;
import com.ject6.boost.infrastructure.blog.client.dto.ProfileEmbeddingResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class OnboardingProfileEmbeddingSyncService {

    private final PythonAiClient pythonAiClient;
    private final OnboardingProfileTextBuilder profileTextBuilder;

    public boolean syncProfileEmbedding(OnboardingResponse response) {
        if (response.getUserId() == null) {
            log.debug("profile embedding skipped: userId is null sessionId={}", response.getSessionId());
            return false;
        }
        try {
            String profileText = profileTextBuilder.build(response);
            ProfileEmbeddingRequest request = new ProfileEmbeddingRequest(
                    response.getUserId(), profileText, "ONBOARDING");
            ProfileEmbeddingResponse result = pythonAiClient.embedOnboardingProfile(request);
            if (result != null && result.stored()) {
                log.info("profile embedding stored userId={} sessionId={}",
                        response.getUserId(), response.getSessionId());
                return true;
            }
            log.warn("profile embedding not stored userId={} sessionId={}",
                    response.getUserId(), response.getSessionId());
            return false;
        } catch (Exception e) {
            log.warn("profile embedding sync failed userId={} sessionId={} err={}",
                    response.getUserId(), response.getSessionId(), e.getMessage());
            return false;
        }
    }
}
