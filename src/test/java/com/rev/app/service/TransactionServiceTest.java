package com.rev.app.service;

import com.rev.app.entity.NotificationType;
import com.rev.app.entity.Role;
import com.rev.app.entity.Transaction;
import com.rev.app.entity.User;
import com.rev.app.exception.ForbiddenOperationException;
import com.rev.app.repository.ITransactionRepository;
import com.rev.app.repository.IUserRepository;
import com.rev.app.service.impl.TransactionServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TransactionServiceTest {

    @Mock
    private ITransactionRepository transactionRepository;
    @Mock
    private IUserRepository userRepository;
    @Mock
    private IWalletService walletService;
    @Mock
    private INotificationService notificationService;

    @InjectMocks
    private TransactionServiceImpl transactionService;

    @Test
    void sendMoney_usesDefaultNoteWhenEmpty() {
        User sender = User.builder().id(1L).email("sender@revpay.com").fullName("Sender").role(Role.PERSONAL).build();
        User receiver = User.builder().id(2L).email("receiver@revpay.com").fullName("Receiver").role(Role.PERSONAL).build();

        when(userRepository.findByEmail("receiver@revpay.com")).thenReturn(Optional.of(receiver));
        when(walletService.hasSufficientFunds(sender, new BigDecimal("10.00"))).thenReturn(true);
        when(transactionRepository.save(any(Transaction.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Transaction saved = transactionService.sendMoney(sender, "receiver@revpay.com", new BigDecimal("10.00"), "   ");

        assertEquals("Money transfer", saved.getNote());
        verify(walletService).debit(sender, new BigDecimal("10.00"));
        verify(walletService).credit(receiver, new BigDecimal("10.00"));
        verify(notificationService).createNotification(eq(sender), any(String.class), eq(NotificationType.TRANSACTION));
        verify(notificationService).createNotification(eq(receiver), any(String.class), eq(NotificationType.TRANSACTION));
    }

    @Test
    void sendMoney_blocksUnverifiedBusinessUser() {
        User businessSender = User.builder()
                .id(11L)
                .email("biz@revpay.com")
                .role(Role.BUSINESS)
                .businessVerified(false)
                .build();

        assertThrows(ForbiddenOperationException.class, () ->
                transactionService.sendMoney(businessSender, "any@revpay.com", new BigDecimal("5.00"), null));
    }
}
