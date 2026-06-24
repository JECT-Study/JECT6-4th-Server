package com.ject6.boost.infrastructure.blog.repository;

import com.ject6.boost.domain.blog.entity.DiagnosisQuota;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface DiagnosisQuotaJpaRepository extends JpaRepository<DiagnosisQuota, Long> {
    Optional<DiagnosisQuota> findByUserId(Long userId);
}
