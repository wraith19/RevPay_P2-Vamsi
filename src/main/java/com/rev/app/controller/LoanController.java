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
import java.util.List;
import com.rev.app.exception.ResourceNotFoundException;
import com.rev.app.exception.ForbiddenOperationException;

@Controller
@RequestMapping("/loans")
@RequiredArgsConstructor
public class LoanController {

    private final IUserService userService;
    private final ILoanService loanService;

    @GetMapping
    public String listLoans(Authentication authentication, Model model) {
        User user = userService.findByEmail(authentication.getName())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        List<Loan> loans = user.getRole() == Role.ADMIN
                ? loanService.getPendingLoans()
                : loanService.getUserLoans(user);
        model.addAttribute("loans", loans);
        model.addAttribute("isAdmin", user.getRole() == Role.ADMIN);
        return "loans/list";
    }

    @GetMapping("/apply")
    public String applyForm(Authentication authentication, RedirectAttributes redirectAttributes) {
        User user = userService.findByEmail(authentication.getName())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        if (user.getRole() != Role.BUSINESS) {
            redirectAttributes.addFlashAttribute("error", "Only business users can apply for loans");
            return "redirect:/loans";
        }
        return "loans/apply";
    }

    @PostMapping("/apply")
    public String applyForLoan(@RequestParam BigDecimal amount,
            @RequestParam String purpose,
            @RequestParam Integer tenureMonths,
            @RequestParam(required = false) String financialInfo,
            @RequestParam(required = false) String supportingDocuments,
            Authentication authentication,
            RedirectAttributes redirectAttributes) {
        try {
            User user = userService.findByEmail(authentication.getName())
                    .orElseThrow(() -> new ResourceNotFoundException("User not found"));

            loanService.applyForLoan(user, amount, purpose, tenureMonths, financialInfo, supportingDocuments);
            redirectAttributes.addFlashAttribute("success", "Loan application submitted successfully!");
            return "redirect:/loans";
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/loans/apply";
        }
    }

    @GetMapping("/{id}")
    public String loanDetail(@PathVariable Long id, Authentication authentication, Model model) {
        User user = userService.findByEmail(authentication.getName())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        Loan loan = loanService.getLoanById(id);
        if (user.getRole() != Role.ADMIN && !loan.getBusinessUser().getId().equals(user.getId())) {
            throw new ForbiddenOperationException("Unauthorized access to loan");
        }
        model.addAttribute("loan", loan);
        model.addAttribute("isAdmin", user.getRole() == Role.ADMIN);
        return "loans/detail";
    }

    @PostMapping("/{id}/repay")
    public String repayLoan(@PathVariable Long id,
            @RequestParam BigDecimal amount,
            Authentication authentication,
            RedirectAttributes redirectAttributes) {
        try {
            User user = userService.findByEmail(authentication.getName())
                    .orElseThrow(() -> new ResourceNotFoundException("User not found"));

            loanService.makeRepayment(id, user, amount);
            redirectAttributes.addFlashAttribute("success", "Repayment successful!");
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/loans/" + id;
    }

    // Simulated auto-approval endpoint
    @PostMapping("/{id}/approve")
    public String approveLoan(@PathVariable Long id,
            @RequestParam(required = false) String adminNote,
            Authentication authentication,
            RedirectAttributes redirectAttributes) {
        try {
            User user = userService.findByEmail(authentication.getName())
                    .orElseThrow(() -> new ResourceNotFoundException("User not found"));
            loanService.approveLoan(id, user, adminNote);
            redirectAttributes.addFlashAttribute("success", "Loan approved!");
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/loans/" + id;
    }

    @PostMapping("/{id}/reject")
    public String rejectLoan(@PathVariable Long id,
            @RequestParam(required = false) String adminNote,
            Authentication authentication,
            RedirectAttributes redirectAttributes) {
        try {
            User user = userService.findByEmail(authentication.getName())
                    .orElseThrow(() -> new ResourceNotFoundException("User not found"));
            loanService.rejectLoan(id, user, adminNote);
            redirectAttributes.addFlashAttribute("success", "Loan rejected!");
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/loans/" + id;
    }
}

