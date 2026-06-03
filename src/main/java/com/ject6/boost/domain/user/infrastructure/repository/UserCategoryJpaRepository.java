package com.ject6.boost.domain.user.infrastructure.repository;

import com.ject6.boost.domain.user.domain.entity.User;
import com.ject6.boost.domain.user.domain.entity.UserCategory;
import com.ject6.boost.domain.user.domain.entity.UserCategoryId;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserCategoryJpaRepository extends JpaRepository<UserCategory, UserCategoryId> {

    List<UserCategory> findByUser(User user);

    void deleteByUser(User user);
}
