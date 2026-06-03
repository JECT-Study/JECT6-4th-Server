package com.ject6.boost.domain.user.infrastructure.repository;

import com.ject6.boost.domain.user.domain.entity.Region;
import com.ject6.boost.domain.user.domain.entity.User;
import com.ject6.boost.domain.user.domain.entity.UserRegion;
import java.time.OffsetDateTime;
import java.util.List;

public interface UserRegionRepository {

    List<UserRegion> findByUser(User user);

    List<UserRegion> saveAll(List<UserRegion> userRegions);

    void replaceAll(User user, List<Region> regions);

    void deleteByUser(User user);

    int softDeleteByUser(User user, OffsetDateTime deletedAt);
}
