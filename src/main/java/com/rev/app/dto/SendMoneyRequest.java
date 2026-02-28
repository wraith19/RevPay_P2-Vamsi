package com.rev.app.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

import java.math.BigDecimal;

public record SendMoneyRequest(
        @NotBlank(message = "Recipient is required")
        String recipient,

        @NotNull(message = "Amount is required")
        @DecimalMin(value = "0.01", message = "Amount must be positive")
        BigDecimal amount,

        String note,

        @NotBlank(message = "Transaction PIN is required")
        @Pattern(regexp = "\\d{4,6}", message = "Transaction PIN must be 4 to 6 digits")
        String transactionPin) {
}
