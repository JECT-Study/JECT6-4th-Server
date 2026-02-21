package com.example.ject6.example.infrastructure.repository;

import com.example.ject6.example.infrastructure.entity.ExampleJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ExampleJpaRepository extends JpaRepository<ExampleJpaEntity, Long>{

}