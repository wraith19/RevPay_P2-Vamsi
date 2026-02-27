package com.rev.app.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Full name is required")
    @Column(nullable = false)
    private String fullName;

    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    @Column(nullable = false, unique = true)
    private String email;

    @NotBlank(message = "Phone number is required")
    @Column(nullable = false, unique = true)
    private String phone;

    @NotBlank(message = "Password is required")
    @Size(min = 6, message = "Password must be at least 6 characters")
    @Column(nullable = false)
    private String password;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;

    // Business-specific fields
    private String businessName;
    private String businessType;
    private String taxId;
    private String businessAddress;
    private String businessContactInfo;
    private Boolean businessVerified;

    // Security
    private String transactionPin;
    private Boolean enabled;

    @Column(updatable = false)
    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    // Notification preferences
    private Boolean notifyTransactions;
    private Boolean notifyRequests;
    private Boolean notifyCardChanges;
    private Boolean notifyLowBalance;
    private Boolean notifyInvoices;
    private Boolean notifyLoans;

    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Wallet wallet;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<PaymentMethod> paymentMethods = new ArrayList<>();

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<SecurityQuestion> securityQuestions = new ArrayList<>();

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (enabled == null)
            enabled = true;
        if (businessVerified == null)
            businessVerified = false;
        if (notifyTransactions == null)
            notifyTransactions = true;
        if (notifyRequests == null)
            notifyRequests = true;
        if (notifyCardChanges == null)
            notifyCardChanges = true;
        if (notifyLowBalance == null)
            notifyLowBalance = true;
        if (notifyInvoices == null)
            notifyInvoices = true;
        if (notifyLoans == null)
            notifyLoans = true;
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
