package com.rev.app.rest;

import com.rev.app.dto.AddFundsRequest;
import com.rev.app.dto.SendMoneyRequest;
import com.rev.app.dto.TransactionResponse;
import com.rev.app.dto.WithdrawFundsRequest;
import com.rev.app.entity.TransactionStatus;
import com.rev.app.entity.TransactionType;
import com.rev.app.entity.User;
import com.rev.app.mapper.TransactionMapper;
import com.rev.app.service.ITransactionService;
import com.rev.app.service.IUserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import com.rev.app.exception.ResourceNotFoundException;

@RestController
@RequestMapping("/api/transactions")
@RequiredArgsConstructor
public class TransactionRestController {

    private final ITransactionService transactionService;
    private final IUserService userService;

    @GetMapping("/my")
    public List<TransactionResponse> getMyTransactions(
            Principal principal,
            @RequestParam(required = false) TransactionType type,
            @RequestParam(required = false) TransactionStatus status,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(required = false) String search) {
        User user = getAuthenticatedUser(principal);

        if (search != null && !search.isBlank()) {
            return transactionService.searchTransactions(user, search)
                    .stream()
                    .map(TransactionMapper::toResponse)
                    .toList();
        }

        if (type != null || status != null || startDate != null || endDate != null) {
            LocalDateTime start = startDate != null ? startDate.atStartOfDay() : null;
            LocalDateTime end = endDate != null ? endDate.atTime(LocalTime.MAX) : null;
            return transactionService.filterTransactions(user, type, status, start, end)
                    .stream()
                    .map(TransactionMapper::toResponse)
                    .toList();
        }

        return transactionService.getTransactionHistory(user)
                .stream()
                .map(TransactionMapper::toResponse)
                .toList();
    }

    @PostMapping("/send")
    public TransactionResponse sendMoney(
            Principal principal,
            @Valid @RequestBody SendMoneyRequest request) {
        User user = getAuthenticatedUser(principal);
        return TransactionMapper.toResponse(
                transactionService.sendMoney(user, request.recipient(), request.amount(), request.note()));
    }

    @PostMapping("/add-funds")
    public TransactionResponse addFunds(
            Principal principal,
            @Valid @RequestBody AddFundsRequest request) {
        User user = getAuthenticatedUser(principal);
        return TransactionMapper.toResponse(
                transactionService.addFunds(user, request.amount(), request.paymentMethodInfo()));
    }

    @PostMapping("/withdraw")
    public TransactionResponse withdraw(
            Principal principal,
            @Valid @RequestBody WithdrawFundsRequest request) {
        User user = getAuthenticatedUser(principal);
        return TransactionMapper.toResponse(
                transactionService.withdrawFunds(user, request.amount()));
    }

    private User getAuthenticatedUser(Principal principal) {
        return userService.findByEmail(principal.getName())
                .orElseThrow(() -> new ResourceNotFoundException("Authenticated user not found"));
    }
}

