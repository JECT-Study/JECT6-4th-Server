package com.ject6.boost.domain.user.domain.repository;

import com.ject6.boost.domain.user.domain.entity.Region;
import java.util.Collection;
import java.util.List;

public interface RegionRepository {

    List<Region> findByIdIn(Collection<Long> ids);
}
