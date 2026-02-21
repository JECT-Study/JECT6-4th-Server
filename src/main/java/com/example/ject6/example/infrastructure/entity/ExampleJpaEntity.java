package com.example.ject6.example.infrastructure.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "example")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
/* Q) createdAt, updatedAt 등의 구현 편의성을 위해 @EntityListeners(AuditingEntityListener.class)를 사용할 지 궁금합니다.
 * */
public class ExampleJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String info;

    public ExampleJpaEntity(Long id, String info) {
        this.id = id;
        this.info = info;
    }

    public ExampleJpaEntity(String info) {
        this.info = info;
    }
}