package com.rev.app.rest;

import com.rev.app.dto.SendMoneyRequest;
import com.rev.app.dto.TransactionResponse;
import com.rev.app.entity.Transaction;
import com.rev.app.entity.TransactionStatus;
import com.rev.app.entity.TransactionType;
import com.rev.app.entity.User;
import com.rev.app.exception.ValidationException;
import com.rev.app.service.ITransactionService;
import com.rev.app.service.IUserService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.security.Principal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TransactionRestControllerTest {

    @Mock
    private ITransactionService transactionService;
    @Mock
    private IUserService userService;

    @InjectMocks
    private TransactionRestController transactionRestController;

    private Principal principal(String name) {
        return () -> name;
    }

    @Test
    void getMyTransactions_returnsMappedHistory() {
        User user = User.builder().id(1L).email("user@revpay.com").transactionPin("ENC").build();
        Transaction transaction = Transaction.builder()
                .id(100L)
                .transactionId("TXN-100")
                .sender(user)
                .amount(new BigDecimal("35.00"))
                .type(TransactionType.SEND)
                .status(TransactionStatus.SUCCESS)
                .note("Sample")
                .build();

        when(userService.findByEmail("user@revpay.com")).thenReturn(Optional.of(user));
        when(transactionService.getTransactionHistory(user)).thenReturn(List.of(transaction));

        List<TransactionResponse> responses = transactionRestController.getMyTransactions(
                principal("user@revpay.com"), null, null, null, null, null, null, null);

        assertEquals(1, responses.size());
        assertEquals("TXN-100", responses.get(0).transactionId());
    }

    @Test
    void sendMoney_throwsWhenTransactionPinMissingOnUser() {
        User user = User.builder().id(1L).email("user@revpay.com").transactionPin(null).build();
        when(userService.findByEmail("user@revpay.com")).thenReturn(Optional.of(user));

        SendMoneyRequest request = new SendMoneyRequest("receiver@revpay.com", new BigDecimal("10.00"), null, "1234");

        assertThrows(ValidationException.class, () ->
                transactionRestController.sendMoney(principal("user@revpay.com"), request));
    }

    @Test
    void sendMoney_callsServiceWhenPinValid() {
        User user = User.builder().id(1L).email("user@revpay.com").transactionPin("ENC").build();
        Transaction transaction = Transaction.builder()
                .id(200L)
                .transactionId("TXN-200")
                .sender(user)
                .amount(new BigDecimal("12.00"))
                .type(TransactionType.SEND)
                .status(TransactionStatus.SUCCESS)
                .build();

        when(userService.findByEmail("user@revpay.com")).thenReturn(Optional.of(user));
        when(userService.verifyTransactionPin(1L, "1234")).thenReturn(true);
        when(transactionService.sendMoney(user, "receiver@revpay.com", new BigDecimal("12.00"), "Lunch"))
                .thenReturn(transaction);

        SendMoneyRequest request = new SendMoneyRequest("receiver@revpay.com", new BigDecimal("12.00"), "Lunch", "1234");
        TransactionResponse response = transactionRestController.sendMoney(principal("user@revpay.com"), request);

        assertEquals("TXN-200", response.transactionId());
        verify(transactionService).sendMoney(user, "receiver@revpay.com", new BigDecimal("12.00"), "Lunch");
        verify(userService).verifyTransactionPin(eq(1L), eq("1234"));
    }
}
