package com.rev.app.service;

import com.rev.app.entity.Transaction;
import com.rev.app.entity.TransactionStatus;
import com.rev.app.entity.TransactionType;
import com.rev.app.entity.User;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public interface ITransactionService {
    Transaction sendMoney(User sender, String recipientIdentifier, BigDecimal amount, String note);

    Transaction addFunds(User user, BigDecimal amount, String paymentMethodInfo);

    Transaction withdrawFunds(User user, BigDecimal amount);

    List<Transaction> getTransactionHistory(User user);

    List<Transaction> filterTransactions(User user, TransactionType type, TransactionStatus status,
                                         LocalDateTime startDate, LocalDateTime endDate,
                                         BigDecimal minAmount, BigDecimal maxAmount);

    List<Transaction> searchTransactions(User user, String searchTerm);

    BigDecimal getTotalReceived(User user);

    BigDecimal getTotalSent(User user);
}
