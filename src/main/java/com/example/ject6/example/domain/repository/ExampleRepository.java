// domain
package com.example.ject6.example.domain.repository;

import com.example.ject6.example.domain.entity.Example;

import java.util.Optional;

public interface ExampleRepository {

    Example save(Example example);

    Optional<Example> findById(Long id);
}