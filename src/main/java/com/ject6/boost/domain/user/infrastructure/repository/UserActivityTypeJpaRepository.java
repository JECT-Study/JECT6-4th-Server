package com.ject6.boost.domain.user.infrastructure.repository;

import com.ject6.boost.domain.user.domain.entity.User;
import com.ject6.boost.domain.user.domain.entity.UserActivityType;
import com.ject6.boost.domain.user.domain.entity.UserActivityTypeId;
import java.time.OffsetDateTime;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface UserActivityTypeJpaRepository extends JpaRepository<UserActivityType, UserActivityTypeId> {

    List<UserActivityType> findByUser(User user);

    void deleteByUser(User user);

    @Modifying
    @Query("""
            update UserActivityType userActivityType
            set userActivityType.deletedAt = :deletedAt
            where userActivityType.user = :user
              and userActivityType.deletedAt is null
            """)
    int softDeleteByUser(@Param("user") User user, @Param("deletedAt") OffsetDateTime deletedAt);
}
