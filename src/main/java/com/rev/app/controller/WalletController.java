package com.rev.app.controller;

import com.rev.app.entity.*;
import com.rev.app.service.*;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import com.rev.app.exception.ResourceNotFoundException;

@Controller
@RequestMapping("/wallet")
@RequiredArgsConstructor
public class WalletController {

    private final IUserService userService;
    private final IWalletService walletService;
    private final ITransactionService transactionService;
    private final IPaymentMethodService paymentMethodService;

    @GetMapping("/add-funds")
    public String addFundsForm(Authentication authentication, Model model) {
        User user = userService.findByEmail(authentication.getName())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        model.addAttribute("balance", walletService.getBalance(user));
        model.addAttribute("paymentMethods", paymentMethodService.getUserPaymentMethods(user));
        return "wallet/add-funds";
    }

    @PostMapping("/add-funds")
    public String addFunds(@RequestParam BigDecimal amount,
            @RequestParam Long paymentMethodId,
            Authentication authentication,
            RedirectAttributes redirectAttributes) {
        try {
            User user = userService.findByEmail(authentication.getName())
                    .orElseThrow(() -> new ResourceNotFoundException("User not found"));

            PaymentMethod pm = paymentMethodService.getById(paymentMethodId);
            transactionService.addFunds(user, amount, pm.getMaskedCardNumber());

            redirectAttributes.addFlashAttribute("success", "Funds added successfully!");
            return "redirect:/dashboard";
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/wallet/add-funds";
        }
    }

    @GetMapping("/withdraw")
    public String withdrawForm(Authentication authentication, Model model) {
        User user = userService.findByEmail(authentication.getName())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        model.addAttribute("balance", walletService.getBalance(user));
        return "wallet/withdraw";
    }

    @PostMapping("/withdraw")
    public String withdraw(@RequestParam BigDecimal amount,
            Authentication authentication,
            RedirectAttributes redirectAttributes) {
        try {
            User user = userService.findByEmail(authentication.getName())
                    .orElseThrow(() -> new ResourceNotFoundException("User not found"));

            transactionService.withdrawFunds(user, amount);

            redirectAttributes.addFlashAttribute("success", "Withdrawal successful!");
            return "redirect:/dashboard";
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/wallet/withdraw";
        }
    }
}

