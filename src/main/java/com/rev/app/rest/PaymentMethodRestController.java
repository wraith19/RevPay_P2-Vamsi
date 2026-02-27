package com.rev.app.rest;

import com.rev.app.dto.AddPaymentMethodRequest;
import com.rev.app.dto.ApiMessageResponse;
import com.rev.app.dto.PaymentMethodResponse;
import com.rev.app.dto.UpdatePaymentMethodRequest;
import com.rev.app.entity.PaymentMethod;
import com.rev.app.entity.User;
import com.rev.app.mapper.PaymentMethodMapper;
import com.rev.app.service.IPaymentMethodService;
import com.rev.app.service.IUserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;
import java.time.LocalDateTime;
import java.util.List;
import com.rev.app.exception.ResourceNotFoundException;

@RestController
@RequestMapping("/api/payment-methods")
@RequiredArgsConstructor
public class PaymentMethodRestController {

    private final IPaymentMethodService paymentMethodService;
    private final IUserService userService;

    @GetMapping("/my")
    public List<PaymentMethodResponse> getMyPaymentMethods(Principal principal) {
        User user = getAuthenticatedUser(principal);
        return paymentMethodService.getUserPaymentMethods(user)
                .stream()
                .map(PaymentMethodMapper::toResponse)
                .toList();
    }

    @PostMapping
    public PaymentMethodResponse addPaymentMethod(
            Principal principal,
            @Valid @RequestBody AddPaymentMethodRequest request) {
        User user = getAuthenticatedUser(principal);
        PaymentMethod created = paymentMethodService.addPaymentMethod(
                user,
                request.cardNumber(),
                request.cardHolderName(),
                request.expiryDate(),
                request.cvv(),
                request.billingAddress(),
                request.type());
        return PaymentMethodMapper.toResponse(created);
    }

    @PutMapping("/{id}")
    public PaymentMethodResponse updatePaymentMethod(
            @PathVariable Long id,
            Principal principal,
            @Valid @RequestBody UpdatePaymentMethodRequest request) {
        User user = getAuthenticatedUser(principal);
        PaymentMethod updated = paymentMethodService.updatePaymentMethod(
                id,
                user,
                request.cardHolderName(),
                request.expiryDate(),
                request.billingAddress());
        return PaymentMethodMapper.toResponse(updated);
    }

    @DeleteMapping("/{id}")
    public ApiMessageResponse deletePaymentMethod(@PathVariable Long id, Principal principal) {
        User user = getAuthenticatedUser(principal);
        paymentMethodService.deletePaymentMethod(id, user);
        return new ApiMessageResponse("Payment method deleted", LocalDateTime.now());
    }

    @PatchMapping("/{id}/default")
    public ApiMessageResponse setDefaultPaymentMethod(@PathVariable Long id, Principal principal) {
        User user = getAuthenticatedUser(principal);
        paymentMethodService.setDefault(id, user);
        return new ApiMessageResponse("Default payment method updated", LocalDateTime.now());
    }

    private User getAuthenticatedUser(Principal principal) {
        return userService.findByEmail(principal.getName())
                .orElseThrow(() -> new ResourceNotFoundException("Authenticated user not found"));
    }
}

