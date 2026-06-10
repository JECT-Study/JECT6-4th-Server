package com.ject6.boost.infrastructure.campaign.repository;

import com.ject6.boost.domain.campaign.constant.UserCampaignStatus;
import com.ject6.boost.domain.campaign.entity.UserCampaign;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.Optional;

public interface UserCampaignJpaRepository extends JpaRepository<UserCampaign, Long> {
    Optional<UserCampaign> findByUserIdAndCampaignId(Long userId, Long campaignId);
    Optional<UserCampaign> findByUserIdAndCampaignIdAndStatus(Long userId, Long campaignId, UserCampaignStatus status);
    List<UserCampaign> findByUserId(Long userId);
    List<UserCampaign> findByUserIdAndStatus(Long userId, UserCampaignStatus status);
    List<UserCampaign> findByCampaignIdAndStatus(Long campaignId, UserCampaignStatus status);
    @Query("""
        select uc.campaignId
        from UserCampaign uc
        where uc.user.id = :userId
            and uc.campaignId in :campaignIds
            and uc.status = :status
        """)
    List<Long> findCampaignIdsByUserIdAndCampaignIdInAndStatus(
        @Param("userId") Long userId,
        @Param("campaignIds") List<Long> campaignIds,
        @Param("status") UserCampaignStatus status);
    boolean existsByUserIdAndCampaignIdAndStatus(Long userId, Long campaignId, UserCampaignStatus status);
    long countByCampaignIdAndStatus(Long campaignId, UserCampaignStatus status);
}
