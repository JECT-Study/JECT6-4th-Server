package com.ject6.boost.domain.user.application.dto;

import com.ject6.boost.domain.user.domain.constant.ActivityType;
import java.util.List;

public record UserMeResponse(
        Long id,
        String nickname,
        boolean onboardingCompleted,
        List<Long> categoryIds,
        List<ActivityType> activityTypes,
        List<Long> regionIds,
        List<ActivityChannelResponse> activityChannels
) {
}
