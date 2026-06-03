package com.ject6.boost.domain.user.infrastructure.repository;

import com.ject6.boost.domain.user.domain.entity.Category;
import com.ject6.boost.domain.user.domain.entity.User;
import com.ject6.boost.domain.user.domain.entity.UserCategory;
import java.time.OffsetDateTime;
import java.util.List;

public interface UserCategoryRepository {

    List<UserCategory> findByUser(User user);

    List<UserCategory> saveAll(List<UserCategory> userCategories);

    void replaceAll(User user, List<Category> categories);

    void deleteByUser(User user);

    int softDeleteByUser(User user, OffsetDateTime deletedAt);
}
