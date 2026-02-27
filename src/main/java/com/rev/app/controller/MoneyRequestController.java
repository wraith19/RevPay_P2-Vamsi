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

@Controller
@RequestMapping("/requests")
@RequiredArgsConstructor
public class MoneyRequestController {

    private final IUserService userService;
    private final IMoneyRequestService moneyRequestService;

    @GetMapping("/create")
    public String createForm() {
        return "requests/create";
    }

    @PostMapping("/create")
    public String createRequest(@RequestParam String requesteeIdentifier,
            @RequestParam BigDecimal amount,
            @RequestParam(required = false) String purpose,
            Authentication authentication,
            RedirectAttributes redirectAttributes) {
        try {
            User requester = userService.findByEmail(authentication.getName())
                    .orElseThrow(() -> new ResourceNotFoundException("User not found"));
            User requestee = userService.findByEmailOrPhone(requesteeIdentifier)
                    .orElseThrow(() -> new ResourceNotFoundException("User not found: " + requesteeIdentifier));

            moneyRequestService.createRequest(requester, requestee, amount, purpose);
            redirectAttributes.addFlashAttribute("success", "Money request sent successfully!");
            return "redirect:/requests/outgoing";
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/requests/create";
        }
    }

    @GetMapping("/incoming")
    public String incomingRequests(Authentication authentication, Model model) {
        User user = userService.findByEmail(authentication.getName())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        List<MoneyRequest> requests = moneyRequestService.getIncomingRequests(user);
        model.addAttribute("requests", requests);
        model.addAttribute("user", user);
        return "requests/incoming";
    }

    @GetMapping("/outgoing")
    public String outgoingRequests(Authentication authentication, Model model) {
        User user = userService.findByEmail(authentication.getName())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        List<MoneyRequest> requests = moneyRequestService.getOutgoingRequests(user);
        model.addAttribute("requests", requests);
        model.addAttribute("user", user);
        return "requests/outgoing";
    }

    @PostMapping("/{id}/accept")
    public String acceptRequest(@PathVariable Long id, Authentication authentication,
            RedirectAttributes redirectAttributes) {
        try {
            User user = userService.findByEmail(authentication.getName())
                    .orElseThrow(() -> new ResourceNotFoundException("User not found"));
            moneyRequestService.acceptRequest(id, user);
            redirectAttributes.addFlashAttribute("success", "Request accepted!");
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/requests/incoming";
    }

    @PostMapping("/{id}/decline")
    public String declineRequest(@PathVariable Long id, Authentication authentication,
            RedirectAttributes redirectAttributes) {
        try {
            User user = userService.findByEmail(authentication.getName())
                    .orElseThrow(() -> new ResourceNotFoundException("User not found"));
            moneyRequestService.declineRequest(id, user);
            redirectAttributes.addFlashAttribute("success", "Request declined!");
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/requests/incoming";
    }

    @PostMapping("/{id}/cancel")
    public String cancelRequest(@PathVariable Long id, Authentication authentication,
            RedirectAttributes redirectAttributes) {
        try {
            User user = userService.findByEmail(authentication.getName())
                    .orElseThrow(() -> new ResourceNotFoundException("User not found"));
            moneyRequestService.cancelRequest(id, user);
            redirectAttributes.addFlashAttribute("success", "Request cancelled!");
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/requests/outgoing";
    }
}

