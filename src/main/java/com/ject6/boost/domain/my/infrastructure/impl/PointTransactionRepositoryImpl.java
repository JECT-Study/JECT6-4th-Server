package com.ject6.boost.domain.my.infrastructure.impl;

import com.ject6.boost.domain.my.domain.entity.PointTransaction;
import com.ject6.boost.domain.my.domain.repository.PointTransactionRepository;
import com.ject6.boost.domain.my.infrastructure.repository.PointTransactionJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class PointTransactionRepositoryImpl implements PointTransactionRepository {
    private final PointTransactionJpaRepository jpa;
    @Override public PointTransaction save(PointTransaction t)                     { return jpa.save(t); }
    @Override public List<PointTransaction> findByUserIdOrderByCreatedAtDesc(Long userId) { return jpa.findByUserIdOrderByCreatedAtDesc(userId); }
}
