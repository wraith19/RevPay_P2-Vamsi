package com.rev.app.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public record InvoiceResponse(
        Long id,
        String invoiceNumber,
        String customerName,
        String customerEmail,
        String customerAddress,
        BigDecimal subtotal,
        BigDecimal taxAmount,
        BigDecimal totalAmount,
        String status,
        LocalDate dueDate,
        String paymentTerms,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        List<InvoiceItemResponse> items) {
}
