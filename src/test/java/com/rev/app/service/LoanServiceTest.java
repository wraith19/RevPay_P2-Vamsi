package com.rev.app.service;

import com.rev.app.entity.Loan;
import com.rev.app.entity.NotificationType;
import com.rev.app.entity.Role;
import com.rev.app.entity.User;
import com.rev.app.exception.ForbiddenOperationException;
import com.rev.app.repository.ILoanRepository;
import com.rev.app.service.impl.LoanServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LoanServiceTest {

    @Mock
    private ILoanRepository loanRepository;

    @Mock
    private IWalletService walletService;

    @Mock
    private INotificationService notificationService;

    @InjectMocks
    private LoanServiceImpl loanService;

    private User verifiedBusinessUser;
    private User unverifiedBusinessUser;

    @BeforeEach
    void setUp() {
        verifiedBusinessUser = User.builder()
                .id(10L)
                .email("verified-business@revpay.com")
                .role(Role.BUSINESS)
                .businessVerified(true)
                .build();

        unverifiedBusinessUser = User.builder()
                .id(11L)
                .email("pending-business@revpay.com")
                .role(Role.BUSINESS)
                .businessVerified(false)
                .build();
    }

    @Test
    void applyForLoan_allowsVerifiedBusinessUser() {
        when(loanRepository.save(any(Loan.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Loan result = loanService.applyForLoan(
                verifiedBusinessUser,
                new BigDecimal("5000.00"),
                "Expansion",
                12,
                "Revenue details",
                "/uploads/loans/sample.pdf");

        assertNotNull(result);
        verify(loanRepository).save(any(Loan.class));
        verify(notificationService).createNotification(
                eq(verifiedBusinessUser),
                any(String.class),
                eq(NotificationType.LOAN));
    }

    @Test
    void applyForLoan_blocksUnverifiedBusinessUser() {
        assertThrows(ForbiddenOperationException.class, () ->
                loanService.applyForLoan(
                        unverifiedBusinessUser,
                        new BigDecimal("5000.00"),
                        "Expansion",
                        12,
                        "Revenue details",
                        "/uploads/loans/sample.pdf"));

        verify(loanRepository, never()).save(any(Loan.class));
    }
}
