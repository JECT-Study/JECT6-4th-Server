package com.ject6.boost.domain.user.infrastructure.repository;

import com.ject6.boost.domain.user.domain.constant.ActivityType;
import com.ject6.boost.domain.user.domain.entity.User;
import com.ject6.boost.domain.user.domain.entity.UserActivityChannel;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface UserActivityChannelJpaRepository extends JpaRepository<UserActivityChannel, Long> {

    List<UserActivityChannel> findByUser(User user);

    Optional<UserActivityChannel> findByUserAndActivityType(User user, ActivityType activityType);

    void deleteByUserAndActivityType(User user, ActivityType activityType);

    @Modifying
    @Query("""
            update UserActivityChannel channel
            set channel.deletedAt = :deletedAt
            where channel.user = :user
              and channel.deletedAt is null
            """)
    int softDeleteByUser(@Param("user") User user, @Param("deletedAt") OffsetDateTime deletedAt);
}
