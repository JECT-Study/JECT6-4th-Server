package com.ject6.boost.domain.user.infrastructure.repository;

import com.ject6.boost.domain.auth.domain.OAuthProvider;
import com.ject6.boost.domain.user.domain.entity.UserOAuthAccount;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserOAuthAccountJpaRepository extends JpaRepository<UserOAuthAccount, Long> {

    Optional<UserOAuthAccount> findByProviderAndProviderUserId(OAuthProvider provider, String providerUserId);
}
