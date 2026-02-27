package com.rev.app.mapper;

import com.rev.app.dto.MoneyRequestResponse;
import com.rev.app.entity.MoneyRequest;

public final class MoneyRequestMapper {

    private MoneyRequestMapper() {
    }

    public static MoneyRequestResponse toResponse(MoneyRequest request) {
        return new MoneyRequestResponse(
                request.getId(),
                request.getRequester() != null ? request.getRequester().getId() : null,
                request.getRequestee() != null ? request.getRequestee().getId() : null,
                request.getAmount(),
                request.getPurpose(),
                request.getStatus() != null ? request.getStatus().name() : null,
                request.getCreatedAt(),
                request.getUpdatedAt());
    }
}
