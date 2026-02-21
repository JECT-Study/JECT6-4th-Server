package com.example.ject6.example.infrastructure.mapper;

import com.example.ject6.example.domain.entity.Example;
import com.example.ject6.example.infrastructure.entity.ExampleJpaEntity;

public class ExampleJpaMapper {

    public static ExampleJpaEntity toEntity(Example domain) {
        return new ExampleJpaEntity(
                domain.getId(),
                domain.getInfo()
        );
    }

    public static Example toDomain(ExampleJpaEntity entity) {
        return Example.of(
                entity.getId(),
                entity.getInfo()
        );
    }
}