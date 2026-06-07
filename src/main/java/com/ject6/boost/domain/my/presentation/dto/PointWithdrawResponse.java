package com.ject6.boost.domain.my.presentation.dto;

import java.time.OffsetDateTime;

public record PointWithdrawResponse(
        Long transactionId,
        int amount,
        String status,
        String bankName,
        String maskedAccountNumber,
        OffsetDateTime requestedAt
) {}
