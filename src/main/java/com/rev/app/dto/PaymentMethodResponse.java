package com.rev.app.dto;

public record PaymentMethodResponse(
        Long id,
        Long userId,
        String maskedCardNumber,
        String cardHolderName,
        String expiryDate,
        String billingAddress,
        String type,
        Boolean isDefault) {
}
