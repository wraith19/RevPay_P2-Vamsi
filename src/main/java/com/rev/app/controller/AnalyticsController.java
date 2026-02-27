package com.rev.app.controller;

import com.rev.app.entity.*;
import com.rev.app.service.*;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import com.rev.app.exception.ResourceNotFoundException;

@Controller
@RequestMapping("/analytics")
@RequiredArgsConstructor
public class AnalyticsController {

    private final IUserService userService;
    private final IAnalyticsService analyticsService;
    private final IWalletService walletService;

    @GetMapping
    public String analyticsDashboard(Authentication authentication, Model model) {
        User user = userService.findByEmail(authentication.getName())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        model.addAttribute("user", user);
        model.addAttribute("balance", walletService.getBalance(user));
        model.addAttribute("transactionSummary", analyticsService.getTransactionSummary(user));
        model.addAttribute("topCustomers", analyticsService.getTopCustomers(user));
        model.addAttribute("invoiceStatusSummary", analyticsService.getInvoiceStatusSummary(user));
        model.addAttribute("outstandingInvoices", analyticsService.getOutstandingInvoiceTotal(user));
        model.addAttribute("paidInvoiceCount", analyticsService.getPaidInvoiceCount(user));
        model.addAttribute("overdueInvoiceCount", analyticsService.getOverdueInvoiceCount(user));

        return "analytics/dashboard";
    }
}

