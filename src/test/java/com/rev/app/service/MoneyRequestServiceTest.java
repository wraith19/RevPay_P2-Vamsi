package com.rev.app.service;

import com.rev.app.entity.*;
import com.rev.app.repository.IMoneyRequestRepository;
import com.rev.app.service.impl.MoneyRequestServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MoneyRequestServiceTest {

    @Mock
    private IMoneyRequestRepository moneyRequestRepository;
    @Mock
    private ITransactionService transactionService;
    @Mock
    private INotificationService notificationService;

    @InjectMocks
    private MoneyRequestServiceImpl moneyRequestService;

    private User requester;
    private User requestee;
    private MoneyRequest pendingRequest;

    @BeforeEach
    void setUp() {
        requester = User.builder().id(1L).email("alice@example.com").fullName("Alice")
                .notifyRequests(true).notifyTransactions(true).build();
        requestee = User.builder().id(2L).email("bob@example.com").fullName("Bob")
                .notifyRequests(true).notifyTransactions(true).build();
        pendingRequest = MoneyRequest.builder()
                .id(1L).requester(requester).requestee(requestee)
                .amount(new BigDecimal("50.00")).purpose("Lunch")
                .status(RequestStatus.PENDING).build();
    }

    @Test
    void testCreateRequest_Success() {
        when(moneyRequestRepository.save(any(MoneyRequest.class))).thenReturn(pendingRequest);

        MoneyRequest result = moneyRequestService.createRequest(requester, requestee,
                new BigDecimal("50.00"), "Lunch");

        assertNotNull(result);
        verify(moneyRequestRepository).save(any(MoneyRequest.class));
        verify(notificationService).createNotification(eq(requestee), anyString(), eq(NotificationType.MONEY_REQUEST));
    }

    @Test
    void testCreateRequest_SelfRequest() {
        assertThrows(RuntimeException.class,
                () -> moneyRequestService.createRequest(requester, requester, new BigDecimal("50.00"), "test"));
    }

    @Test
    void testDeclineRequest_Success() {
        when(moneyRequestRepository.findById(1L)).thenReturn(Optional.of(pendingRequest));
        when(moneyRequestRepository.save(any(MoneyRequest.class))).thenReturn(pendingRequest);

        MoneyRequest result = moneyRequestService.declineRequest(1L, requestee);

        assertEquals(RequestStatus.DECLINED, result.getStatus());
        verify(notificationService).createNotification(eq(requester), anyString(), eq(NotificationType.MONEY_REQUEST));
    }

    @Test
    void testDeclineRequest_NotPending() {
        pendingRequest.setStatus(RequestStatus.ACCEPTED);
        when(moneyRequestRepository.findById(1L)).thenReturn(Optional.of(pendingRequest));

        assertThrows(RuntimeException.class, () -> moneyRequestService.declineRequest(1L, requestee));
    }

    @Test
    void testCancelRequest_Success() {
        when(moneyRequestRepository.findById(1L)).thenReturn(Optional.of(pendingRequest));
        when(moneyRequestRepository.save(any(MoneyRequest.class))).thenReturn(pendingRequest);

        MoneyRequest result = moneyRequestService.cancelRequest(1L, requester);

        assertEquals(RequestStatus.CANCELLED, result.getStatus());
    }

    @Test
    void testCancelRequest_Unauthorized() {
        when(moneyRequestRepository.findById(1L)).thenReturn(Optional.of(pendingRequest));

        assertThrows(RuntimeException.class, () ->
                moneyRequestService.cancelRequest(1L, requestee));
    }
}

