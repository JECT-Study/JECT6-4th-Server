package com.ject6.boost.domain.user.application.dto;

public record NicknameCheckResponse(
        String nickname,
        boolean available
) {
}
