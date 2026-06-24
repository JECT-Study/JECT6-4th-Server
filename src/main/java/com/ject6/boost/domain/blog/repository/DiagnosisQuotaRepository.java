package com.ject6.boost.domain.blog.repository;

import com.ject6.boost.domain.blog.entity.DiagnosisQuota;

import java.util.Optional;

public interface DiagnosisQuotaRepository {
    Optional<DiagnosisQuota> findByUserId(Long userId);
    DiagnosisQuota save(DiagnosisQuota quota);
}
