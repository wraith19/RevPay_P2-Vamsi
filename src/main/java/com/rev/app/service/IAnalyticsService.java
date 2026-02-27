package com.rev.app.service;

import com.rev.app.entity.User;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public interface IAnalyticsService {
    BigDecimal getTotalReceived(User user);

    BigDecimal getTotalSent(User user);

    BigDecimal getPendingAmount(User user);

    BigDecimal getOutstandingInvoiceTotal(User user);

    long getPaidInvoiceCount(User user);

    long getOverdueInvoiceCount(User user);

    Map<String, BigDecimal> getTransactionSummary(User user);

    List<Map<String, Object>> getTopCustomers(User user);

    Map<String, Long> getInvoiceStatusSummary(User user);
}
