package com.rev.app.rest;

import com.rev.app.dto.ApplyLoanRequest;
import com.rev.app.dto.LoanDecisionRequest;
import com.rev.app.dto.LoanRepaymentRequest;
import com.rev.app.dto.LoanResponse;
import com.rev.app.entity.Loan;
import com.rev.app.entity.Role;
import com.rev.app.entity.User;
import com.rev.app.mapper.LoanMapper;
import com.rev.app.service.ILoanService;
import com.rev.app.service.IUserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;
import java.util.List;
import com.rev.app.exception.ResourceNotFoundException;
import com.rev.app.exception.ForbiddenOperationException;

@RestController
@RequestMapping("/api/loans")
@RequiredArgsConstructor
public class LoanRestController {

    private final ILoanService loanService;
    private final IUserService userService;

    @GetMapping("/my")
    public List<LoanResponse> getMyLoans(Principal principal) {
        User user = getAuthenticatedUser(principal);
        ensureBusinessUser(user);
        return loanService.getUserLoans(user)
                .stream()
                .map(LoanMapper::toResponse)
                .toList();
    }

    @GetMapping("/{id}")
    public LoanResponse getLoanById(@PathVariable Long id, Principal principal) {
        User user = getAuthenticatedUser(principal);
        Loan loan = loanService.getLoanById(id);
        if (!loan.getBusinessUser().getId().equals(user.getId()) && user.getRole() != Role.ADMIN) {
            throw new ForbiddenOperationException("Unauthorized access to loan");
        }
        return LoanMapper.toResponse(loan);
    }

    @PostMapping
    public LoanResponse applyForLoan(
            Principal principal,
            @Valid @RequestBody ApplyLoanRequest request) {
        User user = getAuthenticatedUser(principal);
        ensureBusinessUser(user);
        Loan loan = loanService.applyForLoan(
                user,
                request.amount(),
                request.purpose(),
                request.tenureMonths(),
                request.financialInfo(),
                request.supportingDocuments());
        return LoanMapper.toResponse(loan);
    }

    @PostMapping("/{id}/repay")
    public LoanResponse repayLoan(
            @PathVariable Long id,
            Principal principal,
            @Valid @RequestBody LoanRepaymentRequest request) {
        User user = getAuthenticatedUser(principal);
        return LoanMapper.toResponse(loanService.makeRepayment(id, user, request.amount()));
    }

    @PatchMapping("/{id}/approve")
    public LoanResponse approveLoan(
            @PathVariable Long id,
            Principal principal,
            @RequestBody(required = false) @Valid LoanDecisionRequest request) {
        User admin = getAuthenticatedUser(principal);
        ensureAdminUser(admin);
        return LoanMapper.toResponse(loanService.approveLoan(id, admin, request != null ? request.adminNote() : null));
    }

    @PatchMapping("/{id}/reject")
    public LoanResponse rejectLoan(
            @PathVariable Long id,
            Principal principal,
            @RequestBody(required = false) @Valid LoanDecisionRequest request) {
        User admin = getAuthenticatedUser(principal);
        ensureAdminUser(admin);
        return LoanMapper.toResponse(loanService.rejectLoan(id, admin, request != null ? request.adminNote() : null));
    }

    @GetMapping("/pending")
    public List<LoanResponse> getPendingLoans(Principal principal) {
        ensureAdminUser(getAuthenticatedUser(principal));
        return loanService.getPendingLoans()
                .stream()
                .map(LoanMapper::toResponse)
                .toList();
    }

    private User getAuthenticatedUser(Principal principal) {
        return userService.findByEmail(principal.getName())
                .orElseThrow(() -> new ResourceNotFoundException("Authenticated user not found"));
    }

    private void ensureBusinessUser(User user) {
        if (user.getRole() != Role.BUSINESS) {
            throw new ForbiddenOperationException("This endpoint is available only for BUSINESS users");
        }
    }

    private void ensureAdminUser(User user) {
        if (user.getRole() != Role.ADMIN) {
            throw new ForbiddenOperationException("This endpoint is available only for ADMIN users");
        }
    }
}

