package com.rev.app.rest;

import com.rev.app.dto.WalletResponse;
import com.rev.app.entity.User;
import com.rev.app.entity.Wallet;
import com.rev.app.mapper.WalletMapper;
import com.rev.app.service.IUserService;
import com.rev.app.service.IWalletService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.security.Principal;
import com.rev.app.exception.ResourceNotFoundException;

@RestController
@RequestMapping("/api/wallet")
@RequiredArgsConstructor
public class WalletRestController {

    private static final BigDecimal LOW_BALANCE_THRESHOLD = new BigDecimal("100.00");

    private final IUserService userService;
    private final IWalletService walletService;

    @GetMapping("/me")
    public WalletResponse getMyWallet(Principal principal) {
        User user = getAuthenticatedUser(principal);
        Wallet wallet = walletService.getWalletByUser(user);
        return WalletMapper.toResponse(wallet, LOW_BALANCE_THRESHOLD);
    }

    private User getAuthenticatedUser(Principal principal) {
        return userService.findByEmail(principal.getName())
                .orElseThrow(() -> new ResourceNotFoundException("Authenticated user not found"));
    }
}

