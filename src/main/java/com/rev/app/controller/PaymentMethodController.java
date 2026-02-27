package com.rev.app.controller;

import com.rev.app.entity.*;
import com.rev.app.service.*;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import com.rev.app.exception.ResourceNotFoundException;

@Controller
@RequestMapping("/payment-methods")
@RequiredArgsConstructor
public class PaymentMethodController {

    private final IUserService userService;
    private final IPaymentMethodService paymentMethodService;

    @GetMapping
    public String listPaymentMethods(Authentication authentication, Model model) {
        User user = userService.findByEmail(authentication.getName())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        List<PaymentMethod> methods = paymentMethodService.getUserPaymentMethods(user);
        model.addAttribute("paymentMethods", methods);
        return "payment-methods/list";
    }

    @GetMapping("/add")
    public String addForm(Model model) {
        model.addAttribute("types", PaymentMethodType.values());
        return "payment-methods/add";
    }

    @PostMapping("/add")
    public String addPaymentMethod(@RequestParam String cardNumber,
            @RequestParam String cardHolderName,
            @RequestParam String expiryDate,
            @RequestParam String cvv,
            @RequestParam(required = false) String billingAddress,
            @RequestParam PaymentMethodType type,
            Authentication authentication,
            RedirectAttributes redirectAttributes) {
        try {
            User user = userService.findByEmail(authentication.getName())
                    .orElseThrow(() -> new ResourceNotFoundException("User not found"));

            paymentMethodService.addPaymentMethod(user, cardNumber, cardHolderName,
                    expiryDate, cvv, billingAddress, type);
            redirectAttributes.addFlashAttribute("success", "Payment method added!");
            return "redirect:/payment-methods";
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/payment-methods/add";
        }
    }

    @PostMapping("/{id}/delete")
    public String deletePaymentMethod(@PathVariable Long id, Authentication authentication,
            RedirectAttributes redirectAttributes) {
        try {
            User user = userService.findByEmail(authentication.getName())
                    .orElseThrow(() -> new ResourceNotFoundException("User not found"));
            paymentMethodService.deletePaymentMethod(id, user);
            redirectAttributes.addFlashAttribute("success", "Payment method removed!");
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/payment-methods";
    }

    @PostMapping("/{id}/set-default")
    public String setDefault(@PathVariable Long id, Authentication authentication,
            RedirectAttributes redirectAttributes) {
        try {
            User user = userService.findByEmail(authentication.getName())
                    .orElseThrow(() -> new ResourceNotFoundException("User not found"));
            paymentMethodService.setDefault(id, user);
            redirectAttributes.addFlashAttribute("success", "Default payment method updated!");
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/payment-methods";
    }
}

