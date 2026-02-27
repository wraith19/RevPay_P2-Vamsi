package com.rev.app.service;

import com.rev.app.entity.Loan;
import com.rev.app.entity.User;

import java.math.BigDecimal;
import java.util.List;

public interface ILoanService {
    Loan applyForLoan(User businessUser, BigDecimal amount, String purpose,
                      Integer tenureMonths, String financialInfo, String documents);

    Loan approveLoan(Long loanId, User adminUser, String adminNote);

    Loan rejectLoan(Long loanId, User adminUser, String adminNote);

    Loan makeRepayment(Long loanId, User user, BigDecimal amount);

    List<Loan> getUserLoans(User user);

    Loan getLoanById(Long id);

    List<Loan> getPendingLoans();
}
