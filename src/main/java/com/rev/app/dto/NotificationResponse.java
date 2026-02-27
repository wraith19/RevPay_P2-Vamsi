package com.rev.app.dto;

import java.time.LocalDateTime;

public record NotificationResponse(
        Long id,
        Long userId,
        String message,
        String type,
        Boolean isRead,
        LocalDateTime timestamp) {
}
