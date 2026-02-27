package com.rev.app.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record MoneyRequestResponse(
        Long id,
        Long requesterId,
        Long requesteeId,
        BigDecimal amount,
        String purpose,
        String status,
        LocalDateTime createdAt,
        LocalDateTime updatedAt) {
}
