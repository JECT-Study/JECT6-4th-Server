package com.ject6.boost.domain.user.domain.repository;

import com.ject6.boost.domain.user.domain.entity.Category;
import java.util.Collection;
import java.util.List;

public interface CategoryRepository {

    List<Category> findByIdIn(Collection<Long> ids);
}
