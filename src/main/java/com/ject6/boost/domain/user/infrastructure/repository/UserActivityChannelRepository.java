package com.ject6.boost.domain.user.infrastructure.repository;

import com.ject6.boost.domain.user.domain.constant.ActivityType;
import com.ject6.boost.domain.user.domain.entity.User;
import com.ject6.boost.domain.user.domain.entity.UserActivityChannel;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

public interface UserActivityChannelRepository {

    List<UserActivityChannel> findByUser(User user);

    Optional<UserActivityChannel> findByUserAndActivityType(User user, ActivityType activityType);

    UserActivityChannel save(UserActivityChannel userActivityChannel);

    UserActivityChannel saveOrUpdate(User user, ActivityType activityType, String url);

    void deleteByUserAndActivityType(User user, ActivityType activityType);

    int softDeleteByUser(User user, OffsetDateTime deletedAt);
}
