package com.rev.app.dto;

import java.math.BigDecimal;

public record TopCustomerResponse(
        String name,
        BigDecimal totalAmount) {
}
