package com.rev.app.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "loans")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Loan {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "business_user_id", nullable = false)
    private User businessUser;

    @NotNull(message = "Loan amount is required")
    @DecimalMin(value = "1000", message = "Minimum loan amount is 1000")
    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal amount;

    @NotBlank(message = "Loan purpose is required")
    private String purpose;

    @NotNull(message = "Tenure is required")
    @Min(value = 3, message = "Minimum tenure is 3 months")
    private Integer tenureMonths;

    @Column(precision = 5, scale = 2)
    private BigDecimal interestRate;

    @Column(precision = 15, scale = 2)
    private BigDecimal emiAmount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private LoanStatus status;

    @Column(precision = 15, scale = 2)
    private BigDecimal repaidAmount;

    @Column(precision = 15, scale = 2)
    private BigDecimal totalRepayable;

    private String financialInfo;
    private String supportingDocuments;
    @Column(length = 500)
    private String adminNote;

    @Column(updatable = false)
    private LocalDateTime appliedAt;

    private LocalDateTime approvedAt;
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        appliedAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (status == null)
            status = LoanStatus.PENDING;
        if (repaidAmount == null)
            repaidAmount = BigDecimal.ZERO;
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public void calculateEmi() {
        if (amount != null && interestRate != null && tenureMonths != null && tenureMonths > 0) {
            BigDecimal monthlyRate = interestRate.divide(BigDecimal.valueOf(1200), 10, java.math.RoundingMode.HALF_UP);
            if (monthlyRate.compareTo(BigDecimal.ZERO) == 0) {
                this.emiAmount = amount.divide(BigDecimal.valueOf(tenureMonths), 2, java.math.RoundingMode.HALF_UP);
            } else {
                // EMI = P * r * (1+r)^n / ((1+r)^n - 1)
                double r = monthlyRate.doubleValue();
                double p = amount.doubleValue();
                int n = tenureMonths;
                double emi = p * r * Math.pow(1 + r, n) / (Math.pow(1 + r, n) - 1);
                this.emiAmount = BigDecimal.valueOf(emi).setScale(2, java.math.RoundingMode.HALF_UP);
            }
            this.totalRepayable = emiAmount.multiply(BigDecimal.valueOf(tenureMonths));
        }
    }
}
