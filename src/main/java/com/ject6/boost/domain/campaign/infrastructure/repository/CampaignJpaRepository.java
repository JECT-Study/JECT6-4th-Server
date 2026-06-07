package com.ject6.boost.domain.campaign.infrastructure.repository;

import com.ject6.boost.domain.campaign.domain.entity.Campaign;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface CampaignJpaRepository extends JpaRepository<Campaign, Long> {
    Optional<Campaign> findByIdAndDeletedAtIsNull(Long id);
    List<Campaign> findAllByIdInAndDeletedAtIsNull(List<Long> ids);
}
