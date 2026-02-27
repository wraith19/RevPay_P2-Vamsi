package com.rev.app.dto;

public record NotificationPreferencesResponse(
        Boolean notifyTransactions,
        Boolean notifyRequests,
        Boolean notifyCardChanges,
        Boolean notifyLowBalance,
        Boolean notifyInvoices,
        Boolean notifyLoans) {
}
