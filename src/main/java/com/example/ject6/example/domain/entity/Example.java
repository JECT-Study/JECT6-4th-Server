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

    /* 신규 생성
    *  Q) 해당 코드를 AI로 생성해서 여기서 예외를 Throw하는데 실제 예외 전파는 service 단에서만 하도록 제한이 필요할지 궁금합니다.
    *     호출이 controller 단에서 일어나는 경우, @Valid, @isBlanck 형태로 검증이 이루어 지지만
    *     create를 직접 호출하는 경우에 대한 검증을 위해 해당 코드가 존재한다고 생각합니다.
    *     여기서 throw를 할 경우, create 호출 이전에 매번 에러 체킹을 할 필요가 없어 장점이 있지만,
    *     비지니스 로직이 domain에 추가되어 코드 컨벤션에 어긋나는지 궁금합니다.
    * */
    public static Example create(String info) {
        if (info == null || info.isBlank()) {
            throw new IllegalArgumentException("info는 필수입니다.");
        }
        return new Example(null, info);
    }

    // Q) 해당 코드의 필요성 여부가 궁금합니다..
    public static Example of(Long id, String info) {
        return new Example(id, info);
    }
}