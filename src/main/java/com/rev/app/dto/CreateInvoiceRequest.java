package com.rev.app.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public record CreateInvoiceRequest(
        @NotBlank(message = "Customer name is required")
        String customerName,

        String customerEmail,
        String customerAddress,
        LocalDate dueDate,
        String paymentTerms,

        @Valid
        List<InvoiceItemCreateRequest> items) {

    public List<InvoiceItemCreateRequest> itemsOrEmpty() {
        return items == null ? new ArrayList<>() : items;
    }
}
