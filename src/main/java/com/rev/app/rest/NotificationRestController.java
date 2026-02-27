package com.rev.app.rest;

import com.rev.app.dto.ApiMessageResponse;
import com.rev.app.dto.NotificationPreferencesResponse;
import com.rev.app.dto.NotificationResponse;
import com.rev.app.dto.UpdateNotificationPreferencesRequest;
import com.rev.app.entity.User;
import com.rev.app.mapper.NotificationMapper;
import com.rev.app.service.INotificationService;
import com.rev.app.service.IUserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import com.rev.app.exception.ResourceNotFoundException;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationRestController {

    private final INotificationService notificationService;
    private final IUserService userService;

    @GetMapping("/my")
    public List<NotificationResponse> getMyNotifications(Principal principal) {
        User user = getAuthenticatedUser(principal);
        return notificationService.getUserNotifications(user)
                .stream()
                .map(NotificationMapper::toResponse)
                .toList();
    }

    @GetMapping("/my/unread")
    public List<NotificationResponse> getMyUnreadNotifications(Principal principal) {
        User user = getAuthenticatedUser(principal);
        return notificationService.getUnreadNotifications(user)
                .stream()
                .map(NotificationMapper::toResponse)
                .toList();
    }

    @GetMapping("/my/unread-count")
    public Map<String, Long> getUnreadCount(Principal principal) {
        User user = getAuthenticatedUser(principal);
        return Map.of("unreadCount", notificationService.getUnreadCount(user));
    }

    @PatchMapping("/{id}/read")
    public ApiMessageResponse markAsRead(@PathVariable Long id, Principal principal) {
        User user = getAuthenticatedUser(principal);
        notificationService.markAsRead(id, user);
        return new ApiMessageResponse("Notification marked as read", LocalDateTime.now());
    }

    @PatchMapping("/my/read-all")
    public ApiMessageResponse markAllAsRead(Principal principal) {
        User user = getAuthenticatedUser(principal);
        notificationService.markAllAsRead(user);
        return new ApiMessageResponse("All notifications marked as read", LocalDateTime.now());
    }

    @GetMapping("/preferences")
    public NotificationPreferencesResponse getPreferences(Principal principal) {
        User user = getAuthenticatedUser(principal);
        return new NotificationPreferencesResponse(
                user.getNotifyTransactions(),
                user.getNotifyRequests(),
                user.getNotifyCardChanges(),
                user.getNotifyLowBalance(),
                user.getNotifyInvoices(),
                user.getNotifyLoans());
    }

    @PutMapping("/preferences")
    public ApiMessageResponse updatePreferences(
            Principal principal,
            @Valid @RequestBody UpdateNotificationPreferencesRequest request) {
        User user = getAuthenticatedUser(principal);
        userService.updateNotificationPreferences(
                user.getId(),
                request.notifyTransactions(),
                request.notifyRequests(),
                request.notifyCardChanges(),
                request.notifyLowBalance(),
                request.notifyInvoices(),
                request.notifyLoans());
        return new ApiMessageResponse("Notification preferences updated", LocalDateTime.now());
    }

    private User getAuthenticatedUser(Principal principal) {
        return userService.findByEmail(principal.getName())
                .orElseThrow(() -> new ResourceNotFoundException("Authenticated user not found"));
    }
}

