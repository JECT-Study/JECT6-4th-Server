package com.ject6.boost.domain.onboarding.application.service;

import com.ject6.boost.common.exception.BusinessException;
import com.ject6.boost.domain.campaign.domain.entity.Campaign;
import com.ject6.boost.domain.campaign.domain.repository.CampaignRepository;
import com.ject6.boost.domain.onboarding.application.exception.OnboardingErrorCode;
import com.ject6.boost.domain.onboarding.domain.entity.OnboardingResponse;
import com.ject6.boost.domain.onboarding.domain.repository.OnboardingResponseRepository;
import com.ject6.boost.domain.onboarding.presentation.dto.OnboardingRecommendResponse;
import com.ject6.boost.domain.onboarding.presentation.dto.OnboardingStepRequest;
import com.ject6.boost.domain.onboarding.presentation.dto.OnboardingStepResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class OnboardingChatService {

    private final OnboardingResponseRepository onboardingResponseRepository;
    private final CampaignRepository campaignRepository;

    @Transactional
    public OnboardingStepResponse saveStep(OnboardingStepRequest request) {
        if (request.step() < 1 || request.step() > 4) {
            throw new BusinessException(OnboardingErrorCode.INVALID_STEP);
        }

        String requestedSessionId = request.sessionId();
        String resolvedSessionId = (requestedSessionId == null || requestedSessionId.isBlank())
                ? UUID.randomUUID().toString()
                : requestedSessionId;

        OnboardingResponse response = onboardingResponseRepository.findBySessionId(resolvedSessionId)
                .orElseGet(() -> OnboardingResponse.create(resolvedSessionId));

        response.applyStep(request.step(), request.answer());
        onboardingResponseRepository.save(response);

        boolean complete = response.isComplete();
        Integer nextStep = complete ? null : request.step() + 1;
        return new OnboardingStepResponse(resolvedSessionId, request.step(), complete, nextStep);
    }

    @Transactional(readOnly = true)
    public OnboardingRecommendResponse getRecommendations(String sessionId) {
        OnboardingResponse response = onboardingResponseRepository.findBySessionId(sessionId)
                .orElseThrow(() -> new BusinessException(OnboardingErrorCode.SESSION_NOT_FOUND));

        if (!response.isComplete()) {
            throw new BusinessException(OnboardingErrorCode.ONBOARDING_NOT_COMPLETE);
        }

        // step1Answer = 카테고리 기반 캠페인 필터링 (단순 구현)
        List<Campaign> campaigns = campaignRepository.findAllByIdIn(List.of())
                .stream().limit(8).toList();

        List<OnboardingRecommendResponse.CampaignItem> items = campaigns.stream()
                .map(c -> new OnboardingRecommendResponse.CampaignItem(
                        c.getId(), c.getTitle(), c.getCategory(),
                        c.getThumbnailUrl(), c.getApplyEndDate()))
                .toList();

        return new OnboardingRecommendResponse(sessionId, items);
    }

    @Transactional
    public void mergeSession(String sessionId, Long userId) {
        onboardingResponseRepository.findBySessionId(sessionId)
                .ifPresent(r -> {
                    r.mergeUser(userId);
                    onboardingResponseRepository.save(r);
                });
    }
}
