package com.ject6.boost.application.blog.service;

import com.ject6.boost.application.blog.exception.BlogErrorCode;
import com.ject6.boost.application.common.exception.BusinessException;
import com.ject6.boost.domain.blog.entity.DiagnosisQuota;
import com.ject6.boost.domain.blog.repository.DiagnosisQuotaRepository;
import com.ject6.boost.presentation.blog.dto.QuotaResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class DiagnosisQuotaService {

    private final DiagnosisQuotaRepository quotaRepository;

    @Transactional
    public DiagnosisQuota reserveOrThrow(Long userId) {
        DiagnosisQuota quota = quotaRepository.findByUserId(userId)
                .orElseGet(() -> DiagnosisQuota.createDefault(userId));
        quota.resetIfExpired();

        if (!quota.canReserve()) {
            throw new BusinessException(BlogErrorCode.DIAGNOSIS_QUOTA_EXCEEDED);
        }
        quota.reserve();
        return quotaRepository.save(quota);
    }

    /** B-2: correlationId 기반 멱등 차감. 같은 correlationId로 재호출 시 used 중복 증가 방지. */
    @Transactional
    public void confirmUsed(Long userId, String correlationId) {
        quotaRepository.findByUserId(userId).ifPresentOrElse(q -> {
            boolean charged = q.confirmUsed(correlationId);
            quotaRepository.save(q);
            if (charged) {
                log.info("quota confirmed userId={} used={} reserved={} correlationId={}",
                        userId, q.getUsed(), q.getReserved(), correlationId);
            } else {
                log.debug("quota confirmUsed skipped (duplicate correlationId) userId={} correlationId={}",
                        userId, correlationId);
            }
        }, () -> log.warn("quota not found on confirmUsed userId={}", userId));
    }

    @Transactional
    public void releaseReservation(Long userId) {
        quotaRepository.findByUserId(userId).ifPresentOrElse(q -> {
            q.releaseReservation();
            quotaRepository.save(q);
            log.info("quota reservation released userId={} used={} reserved={}", userId, q.getUsed(), q.getReserved());
        }, () -> log.warn("quota not found on releaseReservation userId={}", userId));
    }

    @Transactional(readOnly = true)
    public QuotaResponse getQuota(Long userId) {
        DiagnosisQuota quota = quotaRepository.findByUserId(userId)
                .orElseGet(() -> DiagnosisQuota.createDefault(userId));
        quota.resetIfExpired();
        return new QuotaResponse(quota.getUsed(), quota.getLimitCount(), quota.remaining(), quota.getResetAt());
    }
}
