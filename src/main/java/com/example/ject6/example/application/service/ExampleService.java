package com.example.ject6.example.application.service;

import com.example.ject6.example.domain.entity.Example;
import com.example.ject6.example.domain.repository.ExampleRepository;
import com.example.ject6.example.presentation.dto.request.CreateExampleRequest;
import com.example.ject6.example.presentation.dto.response.GetInfoResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ExampleService {

    private final ExampleRepository exampleRepository;

    @Transactional(readOnly = true)
    public GetInfoResponse getInfo(Long id) {

        /* Q) 각 DTO, Entity의 역할에 대해서 궁금합니다.
        *    컨벤션 패키지 구조를 기반 생각한 흐름은 다음과 같습니다.
        *       1. Domain 계층의 Entity를 기반으로 select
        *       2. (1) 에서 찾은 Entity를 queryDTO로 변환
        *       3. (2) 에서 변환된 queryDTO를 responseDTO로 변환
        *    이 중 (2), (3) 번 과정에서 Entity -> queryDTO -> responseDTO로 변환할 때, queryDTO의 역할이 궁금합니다.
        *    아래 코드는 Entity -> responseDTO의 과정으로만 구현하였습니다.
        * */
        Example example = exampleRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Example not found"));

        return GetInfoResponse.builder()
                .info(example.getInfo())
                .build();
    }

    @Transactional
    public Long postInfo(CreateExampleRequest request) {

        Example example = Example.create(request.getInfo());

        Example saved = exampleRepository.save(example);
        /* Q) 평소 개발 시에는 getInfo()처럼 response를 build해서 return 합니다.
         *    응답 변환을 presentation 계층에서 진행한다 적혀있어서,
         *    commandDTO를 return하는지 responseDTO를 return 하는지 궁금합니다.
         * */
        return saved.getId();
    }
}