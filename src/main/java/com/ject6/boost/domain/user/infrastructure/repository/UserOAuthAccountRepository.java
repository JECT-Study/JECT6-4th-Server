package com.ject6.boost.domain.user.infrastructure.repository;

import com.ject6.boost.domain.auth.domain.OAuthProvider;
import com.ject6.boost.domain.user.domain.entity.User;
import com.ject6.boost.domain.user.domain.entity.UserOAuthAccount;
import java.time.OffsetDateTime;
import java.util.Optional;

public interface UserOAuthAccountRepository {

    Optional<UserOAuthAccount> findActiveByProviderAndProviderUserId(OAuthProvider provider, String providerUserId);

    UserOAuthAccount save(UserOAuthAccount userOAuthAccount);

    int softDeleteByUser(User user, OffsetDateTime deletedAt);
}
