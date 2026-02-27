package com.rev.app.dto;

import java.time.LocalDateTime;

public record ApiErrorResponse(
        String message,
        int status,
        LocalDateTime timestamp) {
}
