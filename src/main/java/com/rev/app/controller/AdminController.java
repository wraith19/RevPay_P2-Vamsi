package com.rev.app.controller;

import com.rev.app.entity.Loan;
import com.rev.app.entity.Role;
import com.rev.app.entity.User;
import com.rev.app.service.ILoanService;
import com.rev.app.service.IUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import com.rev.app.exception.ResourceNotFoundException;
import com.rev.app.exception.ForbiddenOperationException;

@Controller
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminController {

    private final IUserService userService;
    private final ILoanService loanService;

    @GetMapping
    public String adminDashboard(Authentication authentication, Model model) {
        ensureAdmin(authentication);

        List<Loan> pendingLoans = loanService.getPendingLoans();
        long businessUsers = userService.countUsersByRole(Role.BUSINESS);
        long personalUsers = userService.countUsersByRole(Role.PERSONAL);
        long pendingBusinessVerifications = userService.countPendingBusinessVerifications();

        model.addAttribute("pendingLoans", pendingLoans.stream().limit(10).toList());
        model.addAttribute("pendingLoanCount", pendingLoans.size());
        model.addAttribute("pendingBusinessVerifications", pendingBusinessVerifications);
        model.addAttribute("businessUserCount", businessUsers);
        model.addAttribute("personalUserCount", personalUsers);
        model.addAttribute("totalUsers", businessUsers + personalUsers + userService.countUsersByRole(Role.ADMIN));

        return "admin/dashboard";
    }

    @GetMapping("/business-verifications")
    public String businessVerifications(Authentication authentication, Model model) {
        ensureAdmin(authentication);
        model.addAttribute("users", userService.getPendingBusinessVerifications());
        return "admin/business-verifications";
    }

    @PostMapping("/business-verifications/{userId}/approve")
    public String approveBusinessVerification(@PathVariable Long userId,
            Authentication authentication,
            RedirectAttributes redirectAttributes) {
        try {
            ensureAdmin(authentication);
            userService.verifyBusiness(userId);
            redirectAttributes.addFlashAttribute("success", "Business user verified successfully.");
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/business-verifications";
    }

    @GetMapping("/users")
    public String userManagement(Authentication authentication,
            @RequestParam(required = false) Role role,
            Model model) {
        ensureAdmin(authentication);
        List<User> users = role == null ? userService.getAllUsers() : userService.getUsersByRole(role);
        model.addAttribute("users", users);
        model.addAttribute("selectedRole", role);
        model.addAttribute("roles", Role.values());
        return "admin/users";
    }

    @PostMapping("/users/{userId}/status")
    public String updateUserStatus(@PathVariable Long userId,
            @RequestParam boolean enabled,
            Authentication authentication,
            RedirectAttributes redirectAttributes) {
        try {
            ensureAdmin(authentication);
            userService.setUserEnabled(userId, enabled);
            redirectAttributes.addFlashAttribute("success",
                    enabled ? "User account enabled." : "User account disabled.");
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/users";
    }

    private void ensureAdmin(Authentication authentication) {
        User user = userService.findByEmail(authentication.getName())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        if (user.getRole() != Role.ADMIN) {
            throw new ForbiddenOperationException("Unauthorized access");
        }
    }
}
