package com.rev.app.dto;

import java.time.LocalDateTime;

public record UserMeResponse(
        Long id,
        String fullName,
        String email,
        String phone,
        String role,
        String businessName,
        Boolean businessVerified,
        Boolean enabled,
        LocalDateTime createdAt,
        LocalDateTime updatedAt) {
}
