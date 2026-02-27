package com.rev.app.service.impl;

import com.rev.app.entity.*;
import com.rev.app.repository.ILoanRepository;
import com.rev.app.service.ILoanService;
import com.rev.app.service.INotificationService;
import com.rev.app.service.IWalletService;
import lombok.RequiredArgsConstructor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import com.rev.app.exception.ResourceNotFoundException;
import com.rev.app.exception.ValidationException;
import com.rev.app.exception.ForbiddenOperationException;

@Service
@RequiredArgsConstructor
public class LoanServiceImpl implements ILoanService {

    private static final Logger logger = LogManager.getLogger(LoanServiceImpl.class);
    private static final BigDecimal DEFAULT_INTEREST_RATE = new BigDecimal("12.0");

    private final ILoanRepository loanRepository;
    private final IWalletService walletService;
    private final INotificationService notificationService;

    @Override
    @Transactional
    public Loan applyForLoan(User businessUser, BigDecimal amount, String purpose,
                             Integer tenureMonths, String financialInfo, String documents) {
        if (businessUser.getRole() != Role.BUSINESS) {
            throw new ForbiddenOperationException("Only business users can apply for loans");
        }

        Loan loan = Loan.builder()
                .businessUser(businessUser)
                .amount(amount)
                .purpose(purpose)
                .tenureMonths(tenureMonths)
                .interestRate(DEFAULT_INTEREST_RATE)
                .status(LoanStatus.PENDING)
                .financialInfo(financialInfo)
                .supportingDocuments(documents)
                .build();

        loan.calculateEmi();
        loan = loanRepository.save(loan);

        notificationService.createNotification(businessUser,
                "Loan application submitted for $" + amount + ". Application is under review.",
                NotificationType.LOAN);

        logger.info("Loan application submitted by: {} for amount: {}", businessUser.getEmail(), amount);
        return loan;
    }

    @Override
    @Transactional
    public Loan approveLoan(Long loanId, User adminUser, String adminNote) {
        ensureAdminUser(adminUser);
        Loan loan = loanRepository.findById(loanId)
                .orElseThrow(() -> new ResourceNotFoundException("Loan not found"));

        if (loan.getStatus() != LoanStatus.PENDING) {
            throw new ValidationException("Loan is not in pending status");
        }

        loan.setStatus(LoanStatus.APPROVED);
        loan.setApprovedAt(LocalDateTime.now());
        loan.setAdminNote(adminNote);
        loan = loanRepository.save(loan);

        walletService.addFunds(loan.getBusinessUser(), loan.getAmount());

        notificationService.createNotification(loan.getBusinessUser(),
                "Your loan of $" + loan.getAmount() + " has been approved! Amount disbursed to your wallet.",
                NotificationType.LOAN);

        logger.info("Loan {} approved", loanId);
        return loan;
    }

    @Override
    @Transactional
    public Loan rejectLoan(Long loanId, User adminUser, String adminNote) {
        ensureAdminUser(adminUser);
        Loan loan = loanRepository.findById(loanId)
                .orElseThrow(() -> new ResourceNotFoundException("Loan not found"));

        if (loan.getStatus() != LoanStatus.PENDING) {
            throw new ValidationException("Loan is not in pending status");
        }

        loan.setStatus(LoanStatus.REJECTED);
        loan.setAdminNote(adminNote);
        loan = loanRepository.save(loan);

        notificationService.createNotification(loan.getBusinessUser(),
                "Your loan application of $" + loan.getAmount() + " has been rejected."
                        + (adminNote != null && !adminNote.isBlank() ? " Reason: " + adminNote : ""),
                NotificationType.LOAN);

        logger.info("Loan {} rejected", loanId);
        return loan;
    }

    @Override
    @Transactional
    public Loan makeRepayment(Long loanId, User user, BigDecimal amount) {
        Loan loan = loanRepository.findById(loanId)
                .orElseThrow(() -> new ResourceNotFoundException("Loan not found"));

        if (!loan.getBusinessUser().getId().equals(user.getId())) {
            throw new ForbiddenOperationException("Unauthorized access to loan");
        }

        if (loan.getStatus() != LoanStatus.APPROVED && loan.getStatus() != LoanStatus.ACTIVE) {
            throw new ValidationException("Loan is not active");
        }

        if (!walletService.hasSufficientFunds(user, amount)) {
            throw new ValidationException("Insufficient balance for repayment");
        }

        walletService.debit(user, amount);
        loan.setRepaidAmount(loan.getRepaidAmount().add(amount));
        loan.setStatus(LoanStatus.ACTIVE);

        if (loan.getRepaidAmount().compareTo(loan.getTotalRepayable()) >= 0) {
            loan.setStatus(LoanStatus.CLOSED);
            notificationService.createNotification(user,
                    "Congratulations! Your loan has been fully repaid.",
                    NotificationType.LOAN);
        } else {
            notificationService.createNotification(user,
                    "Loan repayment of $" + amount + " received. Remaining: $" +
                            loan.getTotalRepayable().subtract(loan.getRepaidAmount()),
                    NotificationType.LOAN);
        }

        loan = loanRepository.save(loan);
        logger.info("Loan repayment of {} for loan {}", amount, loanId);
        return loan;
    }

    @Override
    public List<Loan> getUserLoans(User user) {
        return loanRepository.findByBusinessUserOrderByAppliedAtDesc(user);
    }

    @Override
    public Loan getLoanById(Long id) {
        return loanRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Loan not found"));
    }

    @Override
    public List<Loan> getPendingLoans() {
        return loanRepository.findByStatus(LoanStatus.PENDING);
    }

    private void ensureAdminUser(User user) {
        if (user == null || user.getRole() != Role.ADMIN) {
            throw new ForbiddenOperationException("Only admin users can make this loan decision");
        }
    }
}
