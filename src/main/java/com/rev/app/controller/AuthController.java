package com.rev.app.controller;

import com.rev.app.entity.*;
import com.rev.app.service.IUserService;
import lombok.RequiredArgsConstructor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.ArrayList;
import java.util.List;

@Controller
@RequiredArgsConstructor
public class AuthController {

    private static final Logger logger = LogManager.getLogger(AuthController.class);
    private final IUserService userService;

    @GetMapping("/login")
    public String loginPage(@RequestParam(value = "error", required = false) String error,
            @RequestParam(value = "logout", required = false) String logout,
            Model model) {
        if (error != null) {
            model.addAttribute("error", "Invalid email or password");
        }
        if (logout != null) {
            model.addAttribute("message", "You have been logged out successfully");
        }
        return "auth/login";
    }

    @GetMapping("/register")
    public String registerPage(Model model) {
        model.addAttribute("roles", Role.values());
        return "auth/register";
    }

    @PostMapping("/register")
    public String register(@RequestParam String fullName,
            @RequestParam String email,
            @RequestParam String phone,
            @RequestParam String password,
            @RequestParam String confirmPassword,
            @RequestParam Role role,
            @RequestParam(required = false) String businessName,
            @RequestParam(required = false) String businessType,
            @RequestParam(required = false) String taxId,
            @RequestParam(required = false) String businessAddress,
            @RequestParam(required = false) String businessContactInfo,
            @RequestParam(required = false) String securityQuestion1,
            @RequestParam(required = false) String securityAnswer1,
            @RequestParam(required = false) String securityQuestion2,
            @RequestParam(required = false) String securityAnswer2,
            RedirectAttributes redirectAttributes) {
        try {
            if (!password.equals(confirmPassword)) {
                redirectAttributes.addFlashAttribute("error", "Passwords do not match");
                return "redirect:/register";
            }

            List<SecurityQuestion> questions = new ArrayList<>();
            if (securityQuestion1 != null && !securityQuestion1.isEmpty()) {
                questions.add(SecurityQuestion.builder().question(securityQuestion1).answer(securityAnswer1).build());
            }
            if (securityQuestion2 != null && !securityQuestion2.isEmpty()) {
                questions.add(SecurityQuestion.builder().question(securityQuestion2).answer(securityAnswer2).build());
            }

            if (role == Role.BUSINESS) {
                userService.registerBusinessUser(fullName, email, phone, password,
                        businessName, businessType, taxId, businessAddress, businessContactInfo, questions);
            } else {
                userService.registerPersonalUser(fullName, email, phone, password, questions);
            }

            redirectAttributes.addFlashAttribute("message", "Registration successful! Please login.");
            logger.info("User registered: {} as {}", email, role);
            return "redirect:/login";

        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/register";
        }
    }

    @GetMapping("/")
    public String home() {
        return "redirect:/login";
    }
}

