package com.rev.app.controller;

import com.rev.app.entity.*;
import com.rev.app.service.*;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import com.rev.app.exception.ResourceNotFoundException;

@Controller
@RequestMapping("/profile")
@RequiredArgsConstructor
public class ProfileController {

    private final IUserService userService;

    @GetMapping
    public String profilePage(Authentication authentication, Model model) {
        User user = userService.findByEmail(authentication.getName())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        model.addAttribute("user", user);
        return "profile/edit";
    }

    @PostMapping("/update")
    public String updateProfile(@RequestParam String fullName,
            @RequestParam String phone,
            @RequestParam(required = false) String businessName,
            @RequestParam(required = false) String businessType,
            @RequestParam(required = false) String businessAddress,
            @RequestParam(required = false) String businessContactInfo,
            Authentication authentication,
            RedirectAttributes redirectAttributes) {
        try {
            User user = userService.findByEmail(authentication.getName())
                    .orElseThrow(() -> new ResourceNotFoundException("User not found"));

            userService.updateProfile(user.getId(), fullName, phone, businessName,
                    businessType, businessAddress, businessContactInfo);
            redirectAttributes.addFlashAttribute("success", "Profile updated successfully!");
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/profile";
    }

    @GetMapping("/change-password")
    public String changePasswordForm() {
        return "profile/change-password";
    }

    @PostMapping("/change-password")
    public String changePassword(@RequestParam String currentPassword,
            @RequestParam String newPassword,
            @RequestParam String confirmPassword,
            Authentication authentication,
            RedirectAttributes redirectAttributes) {
        try {
            if (!newPassword.equals(confirmPassword)) {
                redirectAttributes.addFlashAttribute("error", "New passwords do not match!");
                return "redirect:/profile/change-password";
            }

            User user = userService.findByEmail(authentication.getName())
                    .orElseThrow(() -> new ResourceNotFoundException("User not found"));

            userService.changePassword(user.getId(), currentPassword, newPassword);
            redirectAttributes.addFlashAttribute("success", "Password changed successfully!");
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/profile/change-password";
    }

    @GetMapping("/set-pin")
    public String setPinForm() {
        return "profile/set-pin";
    }

    @PostMapping("/set-pin")
    public String setPin(@RequestParam String pin,
            @RequestParam String confirmPin,
            Authentication authentication,
            RedirectAttributes redirectAttributes) {
        try {
            if (!pin.equals(confirmPin)) {
                redirectAttributes.addFlashAttribute("error", "PINs do not match!");
                return "redirect:/profile/set-pin";
            }

            User user = userService.findByEmail(authentication.getName())
                    .orElseThrow(() -> new ResourceNotFoundException("User not found"));

            userService.setTransactionPin(user.getId(), pin);
            redirectAttributes.addFlashAttribute("success", "Transaction PIN set successfully!");
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/profile/set-pin";
    }
}

