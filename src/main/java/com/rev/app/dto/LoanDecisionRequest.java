package com.rev.app.dto;

import jakarta.validation.constraints.Size;

public record LoanDecisionRequest(
        @Size(max = 500, message = "Admin note can be at most 500 characters")
        String adminNote) {
}
