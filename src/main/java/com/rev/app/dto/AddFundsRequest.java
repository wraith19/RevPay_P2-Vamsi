package com.rev.app.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

import java.math.BigDecimal;

public record AddFundsRequest(
        @NotNull(message = "Amount is required")
        @DecimalMin(value = "0.01", message = "Amount must be positive")
        BigDecimal amount,

        @NotBlank(message = "Payment method info is required")
        String paymentMethodInfo,

        @NotBlank(message = "Transaction PIN is required")
        @Pattern(regexp = "\\d{4,6}", message = "Transaction PIN must be 4 to 6 digits")
        String transactionPin) {
}
