package com.ject6.boost.domain.user.application.dto;

import com.ject6.boost.domain.user.domain.constant.ActivityType;
import java.util.List;

public record OnboardingProfileRequest(
        String nickname,
        List<Long> categoryIds,
        List<ActivityType> activityTypes,
        List<Long> regionIds
) {
}
