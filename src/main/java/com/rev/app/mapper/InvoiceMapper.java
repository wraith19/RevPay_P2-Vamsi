package com.rev.app.mapper;

import com.rev.app.dto.InvoiceItemResponse;
import com.rev.app.dto.InvoiceResponse;
import com.rev.app.entity.Invoice;
import com.rev.app.entity.InvoiceItem;

import java.util.Collections;
import java.util.List;

public final class InvoiceMapper {

    private InvoiceMapper() {
    }

    public static InvoiceResponse toResponse(Invoice invoice) {
        List<InvoiceItemResponse> itemResponses = invoice.getItems() == null
                ? Collections.emptyList()
                : invoice.getItems().stream()
                        .map(InvoiceMapper::toItemResponse)
                        .toList();

        return new InvoiceResponse(
                invoice.getId(),
                invoice.getInvoiceNumber(),
                invoice.getCustomerName(),
                invoice.getCustomerEmail(),
                invoice.getCustomerAddress(),
                invoice.getSubtotal(),
                invoice.getTaxAmount(),
                invoice.getTotalAmount(),
                invoice.getStatus() != null ? invoice.getStatus().name() : null,
                invoice.getDueDate(),
                invoice.getPaymentTerms(),
                invoice.getCreatedAt(),
                invoice.getUpdatedAt(),
                itemResponses);
    }

    private static InvoiceItemResponse toItemResponse(InvoiceItem item) {
        return new InvoiceItemResponse(
                item.getId(),
                item.getDescription(),
                item.getQuantity(),
                item.getUnitPrice(),
                item.getTaxRate(),
                item.getLineTotal(),
                item.getTaxAmount());
    }
}
