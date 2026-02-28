package com.rev.app.service.impl;

import com.rev.app.entity.*;
import com.rev.app.repository.ITransactionRepository;
import com.rev.app.repository.IUserRepository;
import com.rev.app.service.INotificationService;
import com.rev.app.service.ITransactionService;
import com.rev.app.service.IWalletService;
import lombok.RequiredArgsConstructor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;
import com.rev.app.exception.ResourceNotFoundException;
import com.rev.app.exception.ValidationException;

@Service
@RequiredArgsConstructor
public class TransactionServiceImpl implements ITransactionService {

    private static final Logger logger = LogManager.getLogger(TransactionServiceImpl.class);

    private final ITransactionRepository transactionRepository;
    private final IUserRepository userRepository;
    private final IWalletService walletService;
    private final INotificationService notificationService;

    @Override
    @Transactional
    public Transaction sendMoney(User sender, String recipientIdentifier, BigDecimal amount, String note) {
        logger.info("Sending {} from {} to {}", amount, sender.getEmail(), recipientIdentifier);

        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new ValidationException("Amount must be positive");
        }

        User receiver = findRecipient(recipientIdentifier);

        if (sender.getId().equals(receiver.getId())) {
            throw new ValidationException("Cannot send money to yourself");
        }

        if (!walletService.hasSufficientFunds(sender, amount)) {
            throw new ValidationException("Insufficient balance");
        }

        walletService.debit(sender, amount);
        walletService.credit(receiver, amount);

        Transaction sendTxn = Transaction.builder()
                .sender(sender)
                .receiver(receiver)
                .amount(amount)
                .type(TransactionType.SEND)
                .status(TransactionStatus.SUCCESS)
                .note(note)
                .build();
        sendTxn = transactionRepository.save(sendTxn);

        notificationService.createNotification(sender,
                "You sent $" + amount + " to " + receiver.getFullName(),
                NotificationType.TRANSACTION);
        notificationService.createNotification(receiver,
                "You received $" + amount + " from " + sender.getFullName(),
                NotificationType.TRANSACTION);

        if (walletService.isLowBalance(sender)) {
            notificationService.createNotification(sender,
                    "Low balance alert! Your wallet balance is below $100",
                    NotificationType.LOW_BALANCE);
        }

        logger.info("Money sent successfully. Transaction ID: {}", sendTxn.getTransactionId());
        return sendTxn;
    }

    @Override
    @Transactional
    public Transaction addFunds(User user, BigDecimal amount, String paymentMethodInfo) {
        logger.info("Adding funds {} to wallet of {}", amount, user.getEmail());

        walletService.addFunds(user, amount);

        Transaction txn = Transaction.builder()
                .receiver(user)
                .amount(amount)
                .type(TransactionType.ADD_FUNDS)
                .status(TransactionStatus.SUCCESS)
                .note("Added funds from " + paymentMethodInfo)
                .build();
        txn = transactionRepository.save(txn);

        notificationService.createNotification(user,
                "You added $" + amount + " to your wallet",
                NotificationType.TRANSACTION);

        return txn;
    }

    @Override
    @Transactional
    public Transaction withdrawFunds(User user, BigDecimal amount) {
        logger.info("Withdrawing {} from wallet of {}", amount, user.getEmail());

        walletService.withdrawFunds(user, amount);

        Transaction txn = Transaction.builder()
                .sender(user)
                .amount(amount)
                .type(TransactionType.WITHDRAWAL)
                .status(TransactionStatus.SUCCESS)
                .note("Withdrawal to bank account")
                .build();
        txn = transactionRepository.save(txn);

        notificationService.createNotification(user,
                "You withdrew $" + amount + " from your wallet",
                NotificationType.TRANSACTION);

        return txn;
    }

    @Override
    public List<Transaction> getTransactionHistory(User user) {
        return transactionRepository.findByUser(user);
    }

    @Override
    public List<Transaction> filterTransactions(User user, TransactionType type,
                                                TransactionStatus status,
                                                LocalDateTime startDate, LocalDateTime endDate,
                                                BigDecimal minAmount, BigDecimal maxAmount) {
        return transactionRepository.findByFilters(user, type, status, startDate, endDate, minAmount, maxAmount);
    }

    @Override
    public List<Transaction> searchTransactions(User user, String searchTerm) {
        List<Transaction> allTxns = transactionRepository.findByUser(user);
        if (searchTerm == null || searchTerm.isEmpty()) {
            return allTxns;
        }
        String term = searchTerm.toLowerCase();
        return allTxns.stream()
                .filter(t -> {
                    boolean matchesTxnId = t.getTransactionId().toLowerCase().contains(term);
                    boolean matchesSender = t.getSender() != null
                            && t.getSender().getFullName().toLowerCase().contains(term);
                    boolean matchesReceiver = t.getReceiver() != null
                            && t.getReceiver().getFullName().toLowerCase().contains(term);
                    return matchesTxnId || matchesSender || matchesReceiver;
                })
                .collect(Collectors.toList());
    }

    @Override
    public BigDecimal getTotalReceived(User user) {
        return transactionRepository.sumReceivedAmount(user);
    }

    @Override
    public BigDecimal getTotalSent(User user) {
        return transactionRepository.sumSentAmount(user);
    }

    private User findRecipient(String identifier) {
        return userRepository.findByEmail(identifier)
                .or(() -> userRepository.findByPhone(identifier))
                .or(() -> {
                    try {
                        return userRepository.findById(Long.parseLong(identifier));
                    } catch (NumberFormatException e) {
                        return java.util.Optional.empty();
                    }
                })
                .orElseThrow(() -> new ResourceNotFoundException("Recipient not found: " + identifier));
    }
}
