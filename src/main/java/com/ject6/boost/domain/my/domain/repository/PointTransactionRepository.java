package com.ject6.boost.domain.my.domain.repository;

import com.ject6.boost.domain.my.domain.entity.PointTransaction;
import java.util.List;

public interface PointTransactionRepository {
    PointTransaction save(PointTransaction transaction);
    List<PointTransaction> findByUserIdOrderByCreatedAtDesc(Long userId);
}
