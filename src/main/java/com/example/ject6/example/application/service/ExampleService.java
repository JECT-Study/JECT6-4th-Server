package com.example.ject6.example.application.service;

import com.example.ject6.example.application.dto.command.CreateExampleCommandDTO;
import com.example.ject6.example.application.dto.query.ExampleInfoQueryDTO;
import com.example.ject6.example.domain.entity.Example;
import com.example.ject6.example.domain.repository.ExampleRepository;
import com.example.ject6.example.presentation.dto.request.CreateExampleRequest;
import com.example.ject6.example.presentation.dto.response.GetInfoResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ExampleService {

    private final ExampleRepository exampleRepository;

    public GetInfoResponse getInfo(Long id) {

        Example example = exampleRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Example not found"));

        return GetInfoResponse.builder()
                .info(example.getInfo())
                .build();
    }

    // Command
    public Long postInfo(CreateExampleRequest request) {

        Example example = Example.create(request.getInfo());

        Example saved = exampleRepository.save(example);

        return saved.getId();
    }
}