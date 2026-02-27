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
@RequestMapping("/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final IUserService userService;
    private final INotificationService notificationService;

    @GetMapping
    public String listNotifications(Authentication authentication, Model model) {
        User user = userService.findByEmail(authentication.getName())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        List<Notification> notifications = notificationService.getUserNotifications(user);
        long unreadCount = notificationService.getUnreadCount(user);

        model.addAttribute("notifications", notifications);
        model.addAttribute("unreadCount", unreadCount);
        model.addAttribute("user", user);
        return "notifications/list";
    }

    @PostMapping("/{id}/read")
    public String markAsRead(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        notificationService.markAsRead(id);
        return "redirect:/notifications";
    }

    @PostMapping("/mark-all-read")
    public String markAllAsRead(Authentication authentication, RedirectAttributes redirectAttributes) {
        User user = userService.findByEmail(authentication.getName())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        notificationService.markAllAsRead(user);
        redirectAttributes.addFlashAttribute("success", "All notifications marked as read!");
        return "redirect:/notifications";
    }

    @GetMapping("/preferences")
    public String preferencesForm(Authentication authentication, Model model) {
        User user = userService.findByEmail(authentication.getName())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        model.addAttribute("user", user);
        return "notifications/preferences";
    }

    @PostMapping("/preferences")
    public String updatePreferences(@RequestParam(required = false, defaultValue = "false") Boolean notifyTransactions,
            @RequestParam(required = false, defaultValue = "false") Boolean notifyRequests,
            @RequestParam(required = false, defaultValue = "false") Boolean notifyCardChanges,
            @RequestParam(required = false, defaultValue = "false") Boolean notifyLowBalance,
            @RequestParam(required = false, defaultValue = "false") Boolean notifyInvoices,
            @RequestParam(required = false, defaultValue = "false") Boolean notifyLoans,
            Authentication authentication,
            RedirectAttributes redirectAttributes) {
        User user = userService.findByEmail(authentication.getName())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        userService.updateNotificationPreferences(user.getId(), notifyTransactions, notifyRequests,
                notifyCardChanges, notifyLowBalance, notifyInvoices, notifyLoans);

        redirectAttributes.addFlashAttribute("success", "Notification preferences updated!");
        return "redirect:/notifications/preferences";
    }
}

