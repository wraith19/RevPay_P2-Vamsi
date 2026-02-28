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
import com.rev.app.exception.ForbiddenOperationException;

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
            @RequestParam(required = false) String pin,
            Authentication authentication,
            RedirectAttributes redirectAttributes) {
        try {
            User user = userService.findByEmail(authentication.getName())
                    .orElseThrow(() -> new ResourceNotFoundException("User not found"));

            String pinSetupRedirect = ensurePinConfigured(user, redirectAttributes, "/wallet/add-funds");
            if (pinSetupRedirect != null) {
                return pinSetupRedirect;
            }
            if (pin == null || pin.isBlank() || !userService.verifyTransactionPin(user.getId(), pin)) {
                redirectAttributes.addFlashAttribute("error", "Invalid transaction PIN.");
                return "redirect:/wallet/add-funds";
            }

            PaymentMethod pm = paymentMethodService.getById(paymentMethodId);
            if (!pm.getUser().getId().equals(user.getId())) {
                throw new ForbiddenOperationException("Unauthorized access to payment method");
            }
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
            @RequestParam(required = false) String pin,
            Authentication authentication,
            RedirectAttributes redirectAttributes) {
        try {
            User user = userService.findByEmail(authentication.getName())
                    .orElseThrow(() -> new ResourceNotFoundException("User not found"));

            String pinSetupRedirect = ensurePinConfigured(user, redirectAttributes, "/wallet/withdraw");
            if (pinSetupRedirect != null) {
                return pinSetupRedirect;
            }
            if (pin == null || pin.isBlank() || !userService.verifyTransactionPin(user.getId(), pin)) {
                redirectAttributes.addFlashAttribute("error", "Invalid transaction PIN.");
                return "redirect:/wallet/withdraw";
            }

            transactionService.withdrawFunds(user, amount);

            redirectAttributes.addFlashAttribute("success", "Withdrawal successful!");
            return "redirect:/dashboard";
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/wallet/withdraw";
        }
    }

    private String ensurePinConfigured(User user, RedirectAttributes redirectAttributes, String returnTo) {
        if (user.getTransactionPin() == null || user.getTransactionPin().isBlank()) {
            redirectAttributes.addFlashAttribute("error", "Set your transaction PIN before making transactions.");
            return "redirect:/profile/set-pin?returnTo=" + returnTo;
        }
        return null;
    }
}

