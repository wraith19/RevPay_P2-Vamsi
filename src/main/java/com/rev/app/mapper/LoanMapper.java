package com.rev.app.mapper;

import com.rev.app.dto.LoanResponse;
import com.rev.app.entity.Loan;

public final class LoanMapper {

    private LoanMapper() {
    }

    public static LoanResponse toResponse(Loan loan) {
        return new LoanResponse(
                loan.getId(),
                loan.getBusinessUser() != null ? loan.getBusinessUser().getId() : null,
                loan.getAmount(),
                loan.getPurpose(),
                loan.getTenureMonths(),
                loan.getInterestRate(),
                loan.getEmiAmount(),
                loan.getStatus() != null ? loan.getStatus().name() : null,
                loan.getRepaidAmount(),
                loan.getTotalRepayable(),
                loan.getFinancialInfo(),
                loan.getSupportingDocuments(),
                loan.getAdminNote(),
                loan.getAppliedAt(),
                loan.getApprovedAt(),
                loan.getUpdatedAt());
    }
}
