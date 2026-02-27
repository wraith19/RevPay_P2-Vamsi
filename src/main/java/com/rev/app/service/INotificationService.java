package com.rev.app.service;

import com.rev.app.entity.Notification;
import com.rev.app.entity.NotificationType;
import com.rev.app.entity.User;

import java.util.List;

public interface INotificationService {
    Notification createNotification(User user, String message, NotificationType type);

    List<Notification> getUserNotifications(User user);

    List<Notification> getUnreadNotifications(User user);

    long getUnreadCount(User user);

    void markAsRead(Long notificationId);

    void markAsRead(Long notificationId, User user);

    void markAllAsRead(User user);
}
