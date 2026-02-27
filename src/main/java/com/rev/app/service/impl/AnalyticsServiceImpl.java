package com.rev.app.service.impl;

import com.rev.app.entity.Invoice;
import com.rev.app.entity.InvoiceStatus;
import com.rev.app.entity.Transaction;
import com.rev.app.entity.User;
import com.rev.app.repository.IInvoiceRepository;
import com.rev.app.repository.ITransactionRepository;
import com.rev.app.service.IAnalyticsService;
import lombok.RequiredArgsConstructor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AnalyticsServiceImpl implements IAnalyticsService {

    private static final Logger logger = LogManager.getLogger(AnalyticsServiceImpl.class);

    private final ITransactionRepository transactionRepository;
    private final IInvoiceRepository invoiceRepository;

    @Override
    public BigDecimal getTotalReceived(User user) {
        return transactionRepository.sumReceivedAmount(user);
    }

    @Override
    public BigDecimal getTotalSent(User user) {
        return transactionRepository.sumSentAmount(user);
    }

    @Override
    public BigDecimal getPendingAmount(User user) {
        List<Invoice> pending = invoiceRepository.findByBusinessUserAndStatus(user, InvoiceStatus.SENT);
        return pending.stream()
                .map(Invoice::getTotalAmount)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    @Override
    public BigDecimal getOutstandingInvoiceTotal(User user) {
        return invoiceRepository.sumOutstandingInvoiceAmount(user);
    }

    @Override
    public long getPaidInvoiceCount(User user) {
        return invoiceRepository.countByBusinessUserAndStatus(user, InvoiceStatus.PAID);
    }

    @Override
    public long getOverdueInvoiceCount(User user) {
        return invoiceRepository.countByBusinessUserAndStatus(user, InvoiceStatus.OVERDUE);
    }

    @Override
    public Map<String, BigDecimal> getTransactionSummary(User user) {
        Map<String, BigDecimal> summary = new LinkedHashMap<>();
        summary.put("totalReceived", getTotalReceived(user));
        summary.put("totalSent", getTotalSent(user));
        summary.put("pendingAmount", getPendingAmount(user));
        return summary;
    }

    @Override
    public List<Map<String, Object>> getTopCustomers(User user) {
        List<Transaction> received = transactionRepository.findTopTransactionsByReceiver(user);
        Map<String, BigDecimal> customerTotals = new LinkedHashMap<>();

        for (Transaction t : received) {
            if (t.getSender() != null) {
                String name = t.getSender().getFullName();
                customerTotals.merge(name, t.getAmount(), BigDecimal::add);
            }
        }

        return customerTotals.entrySet().stream()
                .sorted(Map.Entry.<String, BigDecimal>comparingByValue().reversed())
                .limit(10)
                .map(e -> {
                    Map<String, Object> map = new LinkedHashMap<>();
                    map.put("name", e.getKey());
                    map.put("totalAmount", e.getValue());
                    return map;
                })
                .collect(Collectors.toList());
    }

    @Override
    public Map<String, Long> getInvoiceStatusSummary(User user) {
        Map<String, Long> summary = new LinkedHashMap<>();
        for (InvoiceStatus status : InvoiceStatus.values()) {
            summary.put(status.name(), invoiceRepository.countByBusinessUserAndStatus(user, status));
        }
        return summary;
    }
}
