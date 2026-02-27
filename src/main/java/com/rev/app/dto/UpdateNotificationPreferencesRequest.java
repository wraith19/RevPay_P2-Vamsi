package com.rev.app.dto;

import jakarta.validation.constraints.NotNull;

public record UpdateNotificationPreferencesRequest(
        @NotNull(message = "notifyTransactions is required")
        Boolean notifyTransactions,

        @NotNull(message = "notifyRequests is required")
        Boolean notifyRequests,

        @NotNull(message = "notifyCardChanges is required")
        Boolean notifyCardChanges,

        @NotNull(message = "notifyLowBalance is required")
        Boolean notifyLowBalance,

        @NotNull(message = "notifyInvoices is required")
        Boolean notifyInvoices,

        @NotNull(message = "notifyLoans is required")
        Boolean notifyLoans) {
}
