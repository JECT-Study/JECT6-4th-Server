package com.ject6.boost.domain.user.infrastructure.impl;

import com.ject6.boost.domain.user.domain.entity.Region;
import com.ject6.boost.domain.user.infrastructure.repository.RegionJpaRepository;
import com.ject6.boost.domain.user.infrastructure.repository.RegionRepository;
import java.util.Collection;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class RegionRepositoryImpl implements RegionRepository {

    private final RegionJpaRepository regionJpaRepository;

    @Override
    public List<Region> findByIdIn(Collection<Long> ids) {
        return regionJpaRepository.findByIdIn(ids);
    }
}
