package com.ject6.boost.domain.user.infrastructure.repository;

import com.ject6.boost.domain.user.domain.entity.User;
import com.ject6.boost.domain.user.domain.entity.UserCategory;
import com.ject6.boost.domain.user.domain.entity.UserCategoryId;
import java.time.OffsetDateTime;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface UserCategoryJpaRepository extends JpaRepository<UserCategory, UserCategoryId> {

    List<UserCategory> findByUser(User user);

    void deleteByUser(User user);

    @Modifying
    @Query("""
            update UserCategory userCategory
            set userCategory.deletedAt = :deletedAt
            where userCategory.user = :user
              and userCategory.deletedAt is null
            """)
    int softDeleteByUser(@Param("user") User user, @Param("deletedAt") OffsetDateTime deletedAt);
}
