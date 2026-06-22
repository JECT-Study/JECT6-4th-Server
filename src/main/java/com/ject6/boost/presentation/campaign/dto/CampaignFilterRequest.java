package com.ject6.boost.presentation.campaign.dto;

import com.ject6.boost.domain.campaign.constant.CampaignCategory;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CampaignFilterRequest {
    private List<CampaignCategory> categories;
    private CampaignCategory category;
    private Long parentRegionId;
    private Long childRegionId;
    private String region;

    public List<CampaignCategory> getCategories() {
        if (categories != null && !categories.isEmpty()) {
            return categories;
        }
        if (category != null) {
            return List.of(category);
        }
        return null;
    }
}
