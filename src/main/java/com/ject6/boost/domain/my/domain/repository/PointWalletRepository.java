package com.ject6.boost.domain.my.domain.repository;

import com.ject6.boost.domain.my.domain.entity.PointWallet;
import java.util.Optional;

public interface PointWalletRepository {
    Optional<PointWallet> findByUserId(Long userId);
    PointWallet save(PointWallet wallet);
}
