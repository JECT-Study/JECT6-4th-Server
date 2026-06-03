package com.ject6.boost.domain.user.presentation.dto;

import com.ject6.boost.domain.user.domain.constant.ActivityType;
import java.util.List;

public record OnboardingProfileResponse(
        Long userId,
        String nickname,
        boolean onboardingCompleted,
        List<Long> categoryIds,
        List<ActivityType> activityTypes,
        List<Long> regionIds
) {
}
