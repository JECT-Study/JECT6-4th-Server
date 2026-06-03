package com.ject6.boost.domain.user.infrastructure.impl;

import com.ject6.boost.domain.auth.domain.OAuthProvider;
import com.ject6.boost.domain.user.domain.entity.UserOAuthAccount;
import com.ject6.boost.domain.user.infrastructure.repository.UserOAuthAccountJpaRepository;
import com.ject6.boost.domain.user.infrastructure.repository.UserOAuthAccountRepository;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class UserOAuthAccountRepositoryImpl implements UserOAuthAccountRepository {

    private final UserOAuthAccountJpaRepository userOAuthAccountJpaRepository;

    @Override
    public Optional<UserOAuthAccount> findByProviderAndProviderUserId(OAuthProvider provider, String providerUserId) {
        return userOAuthAccountJpaRepository.findByProviderAndProviderUserId(provider, providerUserId);
    }

    @Override
    public UserOAuthAccount save(UserOAuthAccount userOAuthAccount) {
        return userOAuthAccountJpaRepository.save(userOAuthAccount);
    }
}
