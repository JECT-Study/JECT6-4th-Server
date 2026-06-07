package com.ject6.boost.domain.my.infrastructure.impl;

import com.ject6.boost.domain.my.domain.entity.PointWallet;
import com.ject6.boost.domain.my.domain.repository.PointWalletRepository;
import com.ject6.boost.domain.my.infrastructure.repository.PointWalletJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class PointWalletRepositoryImpl implements PointWalletRepository {
    private final PointWalletJpaRepository jpa;
    @Override public Optional<PointWallet> findByUserId(Long userId) { return jpa.findByUserId(userId); }
    @Override public PointWallet save(PointWallet wallet)            { return jpa.save(wallet); }
}
