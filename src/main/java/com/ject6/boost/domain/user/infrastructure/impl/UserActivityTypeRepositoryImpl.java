package com.ject6.boost.domain.user.infrastructure.impl;

import com.ject6.boost.domain.user.domain.constant.ActivityType;
import com.ject6.boost.domain.user.domain.entity.User;
import com.ject6.boost.domain.user.domain.entity.UserActivityType;
import com.ject6.boost.domain.user.infrastructure.repository.UserActivityTypeJpaRepository;
import com.ject6.boost.domain.user.infrastructure.repository.UserActivityTypeRepository;
import java.time.OffsetDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class UserActivityTypeRepositoryImpl implements UserActivityTypeRepository {

    private final UserActivityTypeJpaRepository userActivityTypeJpaRepository;

    @Override
    public List<UserActivityType> findByUser(User user) {
        return userActivityTypeJpaRepository.findByUser(user);
    }

    @Override
    public List<UserActivityType> saveAll(List<UserActivityType> userActivityTypes) {
        return userActivityTypeJpaRepository.saveAll(userActivityTypes);
    }

    @Override
    public void replaceAll(User user, List<ActivityType> activityTypes) {
        userActivityTypeJpaRepository.deleteByUser(user);
        userActivityTypeJpaRepository.saveAll(activityTypes.stream()
                .map(activityType -> UserActivityType.create(user, activityType))
                .toList());
    }

    @Override
    public void deleteByUser(User user) {
        userActivityTypeJpaRepository.deleteByUser(user);
    }

    @Override
    public int softDeleteByUser(User user, OffsetDateTime deletedAt) {
        return userActivityTypeJpaRepository.softDeleteByUser(user, deletedAt);
    }
}
