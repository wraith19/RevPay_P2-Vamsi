package com.rev.app.dto;

import com.rev.app.entity.PaymentMethodType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

public record AddPaymentMethodRequest(
        @NotBlank(message = "Card number is required")
        @Pattern(regexp = "\\d{13,19}", message = "Card number must be 13 to 19 digits")
        String cardNumber,

        @NotBlank(message = "Card holder name is required")
        String cardHolderName,

        @NotBlank(message = "Expiry date is required")
        String expiryDate,

        @NotBlank(message = "CVV is required")
        @Pattern(regexp = "\\d{3,4}", message = "CVV must be 3 or 4 digits")
        String cvv,

        String billingAddress,

        @NotNull(message = "Payment method type is required")
        PaymentMethodType type) {
}
