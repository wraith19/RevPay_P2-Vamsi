package com.rev.app.mapper;

import com.rev.app.dto.NotificationResponse;
import com.rev.app.entity.Notification;

public final class NotificationMapper {

    private NotificationMapper() {
    }

    public static NotificationResponse toResponse(Notification notification) {
        return new NotificationResponse(
                notification.getId(),
                notification.getUser() != null ? notification.getUser().getId() : null,
                notification.getMessage(),
                notification.getType() != null ? notification.getType().name() : null,
                notification.getIsRead(),
                notification.getTimestamp());
    }
}
