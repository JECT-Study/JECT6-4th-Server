package com.ject6.boost.domain.campaign.domain.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import java.time.LocalDate;
import java.time.OffsetDateTime;

@Getter
@Entity
@Table(name = "campaigns")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Campaign {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "title", nullable = false)
    private String title;

    @Column(name = "brand_name")
    private String brandName;

    @Column(name = "category", length = 30)
    private String category;

    @Column(name = "campaign_type", length = 30)
    private String campaignType;

    @Column(name = "channel", length = 30)
    private String channel;

    @Column(name = "region", length = 30)
    private String region;

    @Column(name = "thumbnail_url")
    private String thumbnailUrl;

    @Column(name = "apply_end_date")
    private LocalDate applyEndDate;

    @Column(name = "recruit_count")
    private Integer recruitCount;

    @Column(name = "reward_amount")
    private Integer rewardAmount;

    @Column(name = "is_guaranteed")
    private Boolean isGuaranteed;

    @Column(name = "source_url")
    private String sourceUrl;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @Column(name = "deleted_at")
    private OffsetDateTime deletedAt;
}
