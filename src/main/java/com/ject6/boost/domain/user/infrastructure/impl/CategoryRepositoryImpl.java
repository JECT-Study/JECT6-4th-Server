package com.ject6.boost.domain.user.infrastructure.impl;

import com.ject6.boost.domain.user.domain.entity.Category;
import com.ject6.boost.domain.user.infrastructure.repository.CategoryJpaRepository;
import com.ject6.boost.domain.user.infrastructure.repository.CategoryRepository;
import java.util.Collection;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class CategoryRepositoryImpl implements CategoryRepository {

    private final CategoryJpaRepository categoryJpaRepository;

    @Override
    public List<Category> findByIdIn(Collection<Long> ids) {
        return categoryJpaRepository.findByIdIn(ids);
    }
}
