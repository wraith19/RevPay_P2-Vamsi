package com.rev.app.mapper;

import com.rev.app.dto.UserMeResponse;
import com.rev.app.entity.User;

public final class UserMapper {

    private UserMapper() {
    }

    public static UserMeResponse toMeResponse(User user) {
        return new UserMeResponse(
                user.getId(),
                user.getFullName(),
                user.getEmail(),
                user.getPhone(),
                user.getRole() != null ? user.getRole().name() : null,
                user.getBusinessName(),
                user.getBusinessVerified(),
                user.getEnabled(),
                user.getCreatedAt(),
                user.getUpdatedAt());
    }
}
