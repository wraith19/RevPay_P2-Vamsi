package com.rev.app.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record TransactionResponse(
        Long id,
        String transactionId,
        Long senderId,
        Long receiverId,
        BigDecimal amount,
        String type,
        String status,
        String note,
        LocalDateTime timestamp) {
}
