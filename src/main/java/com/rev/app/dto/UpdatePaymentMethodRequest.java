package com.rev.app.dto;

import jakarta.validation.constraints.NotBlank;

public record UpdatePaymentMethodRequest(
        @NotBlank(message = "Card holder name is required")
        String cardHolderName,

        @NotBlank(message = "Expiry date is required")
        String expiryDate,

        String billingAddress) {
}
