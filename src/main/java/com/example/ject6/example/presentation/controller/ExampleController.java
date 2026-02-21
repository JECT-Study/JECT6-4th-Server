package com.example.ject6.example.presentation.controller;

import com.example.ject6.common.dto.ApiResponse;
import com.example.ject6.example.presentation.dto.request.CreateExampleRequest;
import com.example.ject6.example.presentation.dto.response.GetInfoResponse;
import com.example.ject6.example.application.service.ExampleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController("/example")
@Tag(name = "Example", description = "Example API")
@RequiredArgsConstructor
public class ExampleController {
    private final ExampleService exampleService;


    @Operation(summary = "Example 조회", description = "ID로 Example 정보를 조회합니다.")
    @GetMapping("/{id}")
    private ApiResponse<GetInfoResponse> getInfo(
            @PathVariable Long id
    ) {
        GetInfoResponse response = exampleService.getInfo(id);

        return ApiResponse.success(response);
    }

    @PostMapping
    private ApiResponse<Long> createExample(
            @RequestBody CreateExampleRequest request
    ){
        Long id = exampleService.postInfo(request);

        return ApiResponse.success(id);
    }
}
