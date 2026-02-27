package com.rev.app.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

@Entity
@Table(name = "payment_methods")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentMethod {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @NotBlank(message = "Card number is required")
    @Column(nullable = false)
    private String cardNumber;

    @NotBlank(message = "Card holder name is required")
    @Column(nullable = false)
    private String cardHolderName;

    @NotBlank(message = "Expiry date is required")
    @Column(nullable = false)
    private String expiryDate;

    @Column(nullable = false)
    private String cvv;

    private String billingAddress;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaymentMethodType type;

    @Column(nullable = false)
    private Boolean isDefault;

    // Masked card number for display
    @Transient
    public String getMaskedCardNumber() {
        if (cardNumber != null && cardNumber.length() >= 4) {
            return "****-****-****-" + cardNumber.substring(cardNumber.length() - 4);
        }
        return "****";
    }

    @PrePersist
    protected void onCreate() {
        if (isDefault == null)
            isDefault = false;
    }
}
