package com.example.ject6.example.presentation.dto.response;

import com.example.ject6.example.application.dto.query.ExampleInfoQueryDTO;
import lombok.Builder;

@Builder
public class GetInfoResponse {
    private String info;
}
