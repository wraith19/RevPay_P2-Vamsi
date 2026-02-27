package com.rev.app.dto;

import java.time.LocalDateTime;

public record ApiMessageResponse(
        String message,
        LocalDateTime timestamp) {
}
