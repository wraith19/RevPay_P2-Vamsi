package com.rev.app.service.impl;

import com.rev.app.entity.Notification;
import com.rev.app.entity.NotificationType;
import com.rev.app.entity.User;
import com.rev.app.repository.INotificationRepository;
import com.rev.app.service.INotificationService;
import lombok.RequiredArgsConstructor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import com.rev.app.exception.ResourceNotFoundException;

@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements INotificationService {

    private static final Logger logger = LogManager.getLogger(NotificationServiceImpl.class);

    private final INotificationRepository notificationRepository;

    @Override
    @Transactional
    public Notification createNotification(User user, String message, NotificationType type) {
        if (!shouldNotify(user, type)) {
            return null;
        }

        Notification notification = Notification.builder()
                .user(user)
                .message(message)
                .type(type)
                .isRead(false)
                .build();

        notification = notificationRepository.save(notification);
        logger.debug("Notification created for user {}: {}", user.getEmail(), message);
        return notification;
    }

    @Override
    public List<Notification> getUserNotifications(User user) {
        return notificationRepository.findByUserOrderByTimestampDesc(user);
    }

    @Override
    public List<Notification> getUnreadNotifications(User user) {
        return notificationRepository.findByUserAndIsReadFalseOrderByTimestampDesc(user);
    }

    @Override
    public long getUnreadCount(User user) {
        return notificationRepository.countByUserAndIsReadFalse(user);
    }

    @Override
    @Transactional
    public void markAsRead(Long notificationId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new ResourceNotFoundException("Notification not found"));
        notification.setIsRead(true);
        notificationRepository.save(notification);
    }

    @Override
    @Transactional
    public void markAsRead(Long notificationId, User user) {
        Notification notification = notificationRepository.findByIdAndUser(notificationId, user)
                .orElseThrow(() -> new ResourceNotFoundException("Notification not found"));
        notification.setIsRead(true);
        notificationRepository.save(notification);
    }

    @Override
    @Transactional
    public void markAllAsRead(User user) {
        List<Notification> unread = notificationRepository.findByUserAndIsReadFalseOrderByTimestampDesc(user);
        for (Notification n : unread) {
            n.setIsRead(true);
            notificationRepository.save(n);
        }
        logger.info("All notifications marked as read for user: {}", user.getEmail());
    }

    private boolean shouldNotify(User user, NotificationType type) {
        return switch (type) {
            case TRANSACTION -> user.getNotifyTransactions() != null && user.getNotifyTransactions();
            case MONEY_REQUEST -> user.getNotifyRequests() != null && user.getNotifyRequests();
            case CARD_CHANGE -> user.getNotifyCardChanges() != null && user.getNotifyCardChanges();
            case LOW_BALANCE -> user.getNotifyLowBalance() != null && user.getNotifyLowBalance();
            case INVOICE -> user.getNotifyInvoices() != null && user.getNotifyInvoices();
            case LOAN -> user.getNotifyLoans() != null && user.getNotifyLoans();
            case SYSTEM -> true;
        };
    }
}
