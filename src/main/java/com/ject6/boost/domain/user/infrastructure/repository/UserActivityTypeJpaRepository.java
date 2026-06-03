package com.ject6.boost.domain.user.infrastructure.repository;

import com.ject6.boost.domain.user.domain.entity.User;
import com.ject6.boost.domain.user.domain.entity.UserActivityType;
import com.ject6.boost.domain.user.domain.entity.UserActivityTypeId;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserActivityTypeJpaRepository extends JpaRepository<UserActivityType, UserActivityTypeId> {

    List<UserActivityType> findByUser(User user);

    void deleteByUser(User user);
}
