package com.example.ject6.example.infrastructure.mapper;

import com.example.ject6.example.domain.entity.Example;
import com.example.ject6.example.infrastructure.entity.ExampleJpaEntity;

/* Q) Mapper를 static으로 구현했는데 MapStruct 등의 라이브러리 사용 여부가 궁금합니다.
 *   또는 @Component 형태로 해서 respository에 주입해서 사용할 수도 있을 것 같습니다.
 * */
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