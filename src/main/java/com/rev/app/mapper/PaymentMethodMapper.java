package com.rev.app.mapper;

import com.rev.app.dto.PaymentMethodResponse;
import com.rev.app.entity.PaymentMethod;

public final class PaymentMethodMapper {

    private PaymentMethodMapper() {
    }

    public static PaymentMethodResponse toResponse(PaymentMethod paymentMethod) {
        return new PaymentMethodResponse(
                paymentMethod.getId(),
                paymentMethod.getUser() != null ? paymentMethod.getUser().getId() : null,
                paymentMethod.getMaskedCardNumber(),
                paymentMethod.getCardHolderName(),
                paymentMethod.getExpiryDate(),
                paymentMethod.getBillingAddress(),
                paymentMethod.getType() != null ? paymentMethod.getType().name() : null,
                paymentMethod.getIsDefault());
    }
}
