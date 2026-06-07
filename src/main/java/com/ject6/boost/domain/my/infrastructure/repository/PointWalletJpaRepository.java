package com.ject6.boost.domain.my.infrastructure.repository;

import com.ject6.boost.domain.my.domain.entity.PointWallet;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface PointWalletJpaRepository extends JpaRepository<PointWallet, Long> {
    Optional<PointWallet> findByUserId(Long userId);
}
