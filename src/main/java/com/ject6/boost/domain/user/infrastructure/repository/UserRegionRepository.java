package com.ject6.boost.domain.user.infrastructure.repository;

import com.ject6.boost.domain.user.domain.entity.User;
import com.ject6.boost.domain.user.domain.entity.UserRegion;
import com.ject6.boost.domain.user.domain.entity.UserRegionId;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRegionRepository extends JpaRepository<UserRegion, UserRegionId> {

    List<UserRegion> findByUser(User user);

    void deleteByUser(User user);
}
