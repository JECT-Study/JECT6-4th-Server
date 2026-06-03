package com.ject6.boost.domain.user.infrastructure.impl;

import com.ject6.boost.domain.auth.domain.OAuthProvider;
import com.ject6.boost.domain.user.domain.entity.User;
import com.ject6.boost.domain.user.domain.entity.UserOAuthAccount;
import com.ject6.boost.domain.user.infrastructure.repository.UserOAuthAccountJpaRepository;
import com.ject6.boost.domain.user.infrastructure.repository.UserOAuthAccountRepository;
import java.time.OffsetDateTime;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class UserOAuthAccountRepositoryImpl implements UserOAuthAccountRepository {

    private final UserOAuthAccountJpaRepository userOAuthAccountJpaRepository;

    @Override
    public Optional<UserOAuthAccount> findActiveByProviderAndProviderUserId(OAuthProvider provider, String providerUserId) {
        Optional<UserOAuthAccount> account =
                userOAuthAccountJpaRepository.findActiveByProviderAndProviderUserId(provider, providerUserId);

        if (account.isPresent()) {
            return account;
        }

        userOAuthAccountJpaRepository.softDeleteActiveAccountOfDeletedUser(provider, providerUserId, OffsetDateTime.now());
        return Optional.empty();
    }

    @Override
    public UserOAuthAccount save(UserOAuthAccount userOAuthAccount) {
        return userOAuthAccountJpaRepository.save(userOAuthAccount);
    }

    @Override
    public int softDeleteByUser(User user, OffsetDateTime deletedAt) {
        return userOAuthAccountJpaRepository.softDeleteByUser(user, deletedAt);
    }
}
