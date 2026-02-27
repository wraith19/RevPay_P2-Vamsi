package com.rev.app.mapper;

import com.rev.app.dto.AnalyticsResponse;
import com.rev.app.dto.TopCustomerResponse;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public final class AnalyticsMapper {

    private AnalyticsMapper() {
    }

    public static AnalyticsResponse toResponse(
            BigDecimal balance,
            Map<String, BigDecimal> transactionSummary,
            BigDecimal outstandingInvoices,
            long paidInvoiceCount,
            long overdueInvoiceCount,
            List<Map<String, Object>> topCustomers,
            Map<String, Long> invoiceStatusSummary) {

        List<TopCustomerResponse> topCustomerResponses = topCustomers.stream()
                .map(AnalyticsMapper::toTopCustomerResponse)
                .toList();

        return new AnalyticsResponse(
                balance,
                transactionSummary.getOrDefault("totalReceived", BigDecimal.ZERO),
                transactionSummary.getOrDefault("totalSent", BigDecimal.ZERO),
                transactionSummary.getOrDefault("pendingAmount", BigDecimal.ZERO),
                outstandingInvoices,
                paidInvoiceCount,
                overdueInvoiceCount,
                topCustomerResponses,
                invoiceStatusSummary);
    }

    private static TopCustomerResponse toTopCustomerResponse(Map<String, Object> customerMap) {
        Object name = customerMap.get("name");
        Object totalAmount = customerMap.get("totalAmount");

        return new TopCustomerResponse(
                name != null ? name.toString() : null,
                totalAmount instanceof BigDecimal ? (BigDecimal) totalAmount : BigDecimal.ZERO);
    }
}
