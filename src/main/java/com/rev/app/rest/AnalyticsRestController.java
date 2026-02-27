package com.rev.app.rest;

import com.rev.app.dto.AnalyticsResponse;
import com.rev.app.entity.Role;
import com.rev.app.entity.User;
import com.rev.app.mapper.AnalyticsMapper;
import com.rev.app.service.IAnalyticsService;
import com.rev.app.service.IUserService;
import com.rev.app.service.IWalletService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;
import java.util.Map;
import com.rev.app.exception.ResourceNotFoundException;
import com.rev.app.exception.ForbiddenOperationException;

@RestController
@RequestMapping("/api/analytics")
@RequiredArgsConstructor
public class AnalyticsRestController {

    private final IUserService userService;
    private final IWalletService walletService;
    private final IAnalyticsService analyticsService;

    @GetMapping("/my")
    public AnalyticsResponse getMyAnalytics(Principal principal) {
        User user = getAuthenticatedUser(principal);
        if (user.getRole() != Role.BUSINESS) {
            throw new ForbiddenOperationException("This endpoint is available only for BUSINESS users");
        }

        Map<String, java.math.BigDecimal> transactionSummary = analyticsService.getTransactionSummary(user);

        return AnalyticsMapper.toResponse(
                walletService.getBalance(user),
                transactionSummary,
                analyticsService.getOutstandingInvoiceTotal(user),
                analyticsService.getPaidInvoiceCount(user),
                analyticsService.getOverdueInvoiceCount(user),
                analyticsService.getTopCustomers(user),
                analyticsService.getInvoiceStatusSummary(user));
    }

    private User getAuthenticatedUser(Principal principal) {
        return userService.findByEmail(principal.getName())
                .orElseThrow(() -> new ResourceNotFoundException("Authenticated user not found"));
    }
}

