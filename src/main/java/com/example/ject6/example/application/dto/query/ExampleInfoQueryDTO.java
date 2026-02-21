package com.example.ject6.example.application.dto.query;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ExampleInfoQueryDTO {

    private Long id;
    private String info;
}