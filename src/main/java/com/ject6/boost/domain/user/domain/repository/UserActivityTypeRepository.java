package com.ject6.boost.domain.user.domain.repository;

import com.ject6.boost.domain.user.domain.constant.ActivityType;
import com.ject6.boost.domain.user.domain.entity.User;
import com.ject6.boost.domain.user.domain.entity.UserActivityType;
import java.time.OffsetDateTime;
import java.util.List;

public interface UserActivityTypeRepository {

    List<UserActivityType> findByUser(User user);

    List<UserActivityType> saveAll(List<UserActivityType> userActivityTypes);

    void replaceAll(User user, List<ActivityType> activityTypes);

    void deleteByUser(User user);

    int softDeleteByUser(User user, OffsetDateTime deletedAt);
}
