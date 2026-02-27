package com.rev.app.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record ApplyLoanRequest(
        @NotNull(message = "Amount is required")
        @DecimalMin(value = "1000.00", message = "Minimum loan amount is 1000")
        BigDecimal amount,

        @NotBlank(message = "Purpose is required")
        String purpose,

        @NotNull(message = "Tenure is required")
        Integer tenureMonths,

        String financialInfo,
        String supportingDocuments) {
}
