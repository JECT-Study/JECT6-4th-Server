package com.example.ject6.example.infrastructure.repository;

import com.example.ject6.example.domain.entity.Example;
import com.example.ject6.example.domain.repository.ExampleRepository;
import com.example.ject6.example.infrastructure.entity.ExampleJpaEntity;
import com.example.ject6.example.infrastructure.mapper.ExampleJpaMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class ExampleRepositoryImpl implements ExampleRepository {

    private final ExampleJpaRepository jpaRepository;

    @Override
    public Example save(Example example) {

        ExampleJpaEntity saved =
                jpaRepository.save(
                        ExampleJpaMapper.toEntity(example)
                );

        return ExampleJpaMapper.toDomain(saved);
    }

    @Override
    public Optional<Example> findById(Long id) {
        return jpaRepository.findById(id)
                .map(ExampleJpaMapper::toDomain);
    }
}