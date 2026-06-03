package com.ject6.boost.domain.user.presentation.dto;

public record NicknameCheckResponse(
        String nickname,
        boolean available
) {
}
