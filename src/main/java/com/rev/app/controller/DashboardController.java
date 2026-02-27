package com.rev.app.controller;

import com.rev.app.entity.*;
import com.rev.app.service.*;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.math.BigDecimal;
import java.util.List;
import com.rev.app.exception.ResourceNotFoundException;

@Controller
@RequiredArgsConstructor
public class DashboardController {

    private final IUserService userService;
    private final IWalletService walletService;
    private final ITransactionService transactionService;
    private final IMoneyRequestService moneyRequestService;
    private final INotificationService notificationService;
    private final IAnalyticsService analyticsService;

    @GetMapping("/dashboard")
    public String dashboard(Authentication authentication, Model model) {
        User user = userService.findByEmail(authentication.getName())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        BigDecimal balance = walletService.getBalance(user);
        List<Transaction> recentTransactions = transactionService.getTransactionHistory(user);
        long pendingRequests = moneyRequestService.getPendingRequestCount(user);
        long unreadNotifications = notificationService.getUnreadCount(user);

        model.addAttribute("user", user);
        model.addAttribute("balance", balance);
        model.addAttribute("recentTransactions", recentTransactions.stream().limit(5).toList());
        model.addAttribute("pendingRequests", pendingRequests);
        model.addAttribute("unreadNotifications", unreadNotifications);

        // Business-specific
        if (user.getRole() == Role.BUSINESS) {
            model.addAttribute("totalReceived", analyticsService.getTotalReceived(user));
            model.addAttribute("totalSent", analyticsService.getTotalSent(user));
            model.addAttribute("pendingAmount", analyticsService.getPendingAmount(user));
        }

        return "dashboard";
    }
}

