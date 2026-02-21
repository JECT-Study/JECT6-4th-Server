package com.example.ject6.example.domain.entity;

import lombok.Getter;

@Getter
public class Example {

    private final Long id;
    private final String info;

    private Example(Long id, String info) {
        this.id = id;
        this.info = info;
    }

    // 신규 생성
    public static Example create(String info) {
        if (info == null || info.isBlank()) {
            throw new IllegalArgumentException("info는 필수입니다.");
        }
        return new Example(null, info);
    }

    // DB 복원용
    public static Example of(Long id, String info) {
        return new Example(id, info);
    }
}