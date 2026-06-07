package com.ject6.boost.domain.campaign.infrastructure.impl;

import com.ject6.boost.domain.campaign.domain.entity.Campaign;
import com.ject6.boost.domain.campaign.domain.repository.CampaignRepository;
import com.ject6.boost.domain.campaign.infrastructure.repository.CampaignJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class CampaignRepositoryImpl implements CampaignRepository {

    private final CampaignJpaRepository jpaRepository;

    @Override
    public Optional<Campaign> findActiveById(Long id) {
        return jpaRepository.findByIdAndDeletedAtIsNull(id);
    }

    @Override
    public List<Campaign> findAllByIdIn(List<Long> ids) {
        return jpaRepository.findAllByIdInAndDeletedAtIsNull(ids);
    }
}
