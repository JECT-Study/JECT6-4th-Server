package com.ject6.boost.domain.user.presentation.dto;

import com.ject6.boost.domain.user.domain.constant.ActivityType;

public record ActivityChannelResponse(
        Long id,
        ActivityType activityType,
        String url
) {
}
