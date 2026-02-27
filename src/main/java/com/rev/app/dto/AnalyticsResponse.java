package com.rev.app.dto;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public record AnalyticsResponse(
        BigDecimal balance,
        BigDecimal totalReceived,
        BigDecimal totalSent,
        BigDecimal pendingAmount,
        BigDecimal outstandingInvoices,
        long paidInvoiceCount,
        long overdueInvoiceCount,
        List<TopCustomerResponse> topCustomers,
        Map<String, Long> invoiceStatusSummary) {
}
