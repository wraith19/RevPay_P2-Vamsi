package com.rev.app.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record LoanResponse(
        Long id,
        Long businessUserId,
        BigDecimal amount,
        String purpose,
        Integer tenureMonths,
        BigDecimal interestRate,
        BigDecimal emiAmount,
        String status,
        BigDecimal repaidAmount,
        BigDecimal totalRepayable,
        String financialInfo,
        String supportingDocuments,
        String adminNote,
        LocalDateTime appliedAt,
        LocalDateTime approvedAt,
        LocalDateTime updatedAt) {
}
