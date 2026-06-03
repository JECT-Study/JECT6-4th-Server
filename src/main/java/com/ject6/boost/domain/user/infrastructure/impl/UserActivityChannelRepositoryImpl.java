package com.ject6.boost.domain.user.infrastructure.impl;

import com.ject6.boost.domain.user.domain.constant.ActivityType;
import com.ject6.boost.domain.user.domain.entity.User;
import com.ject6.boost.domain.user.domain.entity.UserActivityChannel;
import com.ject6.boost.domain.user.infrastructure.repository.UserActivityChannelJpaRepository;
import com.ject6.boost.domain.user.domain.repository.UserActivityChannelRepository;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class UserActivityChannelRepositoryImpl implements UserActivityChannelRepository {

    private final UserActivityChannelJpaRepository userActivityChannelJpaRepository;

    @Override
    public List<UserActivityChannel> findByUser(User user) {
        return userActivityChannelJpaRepository.findByUser(user);
    }

    @Override
    public Optional<UserActivityChannel> findByUserAndActivityType(User user, ActivityType activityType) {
        return userActivityChannelJpaRepository.findByUserAndActivityType(user, activityType);
    }

    @Override
    public UserActivityChannel save(UserActivityChannel userActivityChannel) {
        return userActivityChannelJpaRepository.save(userActivityChannel);
    }

    @Override
    public UserActivityChannel saveOrUpdate(User user, ActivityType activityType, String url) {
        UserActivityChannel channel = userActivityChannelJpaRepository
                .findByUserAndActivityType(user, activityType)
                .orElseGet(() -> userActivityChannelJpaRepository.save(
                        UserActivityChannel.create(user, activityType, url)
                ));
        channel.updateUrl(url);
        return channel;
    }

    @Override
    public void deleteByUserAndActivityType(User user, ActivityType activityType) {
        userActivityChannelJpaRepository.deleteByUserAndActivityType(user, activityType);
    }

    @Override
    public int softDeleteByUser(User user, OffsetDateTime deletedAt) {
        return userActivityChannelJpaRepository.softDeleteByUser(user, deletedAt);
    }
}
