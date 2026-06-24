package com.ject6.boost.infrastructure.blog.impl;

import com.ject6.boost.domain.blog.entity.DiagnosisQuota;
import com.ject6.boost.domain.blog.repository.DiagnosisQuotaRepository;
import com.ject6.boost.infrastructure.blog.repository.DiagnosisQuotaJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class DiagnosisQuotaRepositoryImpl implements DiagnosisQuotaRepository {

    private final DiagnosisQuotaJpaRepository jpa;

    @Override
    public Optional<DiagnosisQuota> findByUserId(Long userId) {
        return jpa.findByUserId(userId);
    }

    @Override
    public DiagnosisQuota save(DiagnosisQuota quota) {
        return jpa.save(quota);
    }
}
