package com.ject6.boost.domain.user.domain.entity;

import com.ject6.boost.domain.user.domain.constant.ActivityType;
import java.io.Serializable;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@EqualsAndHashCode
public class UserActivityTypeId implements Serializable {

    private Long user;
    private ActivityType activityType;
}
