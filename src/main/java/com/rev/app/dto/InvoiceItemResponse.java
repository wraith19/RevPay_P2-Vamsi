package com.rev.app.dto;

import java.math.BigDecimal;

public record InvoiceItemResponse(
        Long id,
        String description,
        Integer quantity,
        BigDecimal unitPrice,
        BigDecimal taxRate,
        BigDecimal lineTotal,
        BigDecimal taxAmount) {
}
