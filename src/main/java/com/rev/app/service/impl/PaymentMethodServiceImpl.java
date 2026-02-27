package com.rev.app.service.impl;

import com.rev.app.entity.NotificationType;
import com.rev.app.entity.PaymentMethod;
import com.rev.app.entity.PaymentMethodType;
import com.rev.app.entity.User;
import com.rev.app.repository.IPaymentMethodRepository;
import com.rev.app.service.INotificationService;
import com.rev.app.service.IPaymentMethodService;
import lombok.RequiredArgsConstructor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import com.rev.app.exception.ResourceNotFoundException;
import com.rev.app.exception.ForbiddenOperationException;

@Service
@RequiredArgsConstructor
public class PaymentMethodServiceImpl implements IPaymentMethodService {

    private static final Logger logger = LogManager.getLogger(PaymentMethodServiceImpl.class);

    private final IPaymentMethodRepository paymentMethodRepository;
    private final INotificationService notificationService;

    @Override
    public List<PaymentMethod> getUserPaymentMethods(User user) {
        return paymentMethodRepository.findByUser(user);
    }

    @Override
    @Transactional
    public PaymentMethod addPaymentMethod(User user, String cardNumber, String cardHolderName,
                                          String expiryDate, String cvv, String billingAddress,
                                          PaymentMethodType type) {
        PaymentMethod pm = PaymentMethod.builder()
                .user(user)
                .cardNumber(cardNumber)
                .cardHolderName(cardHolderName)
                .expiryDate(expiryDate)
                .cvv(cvv)
                .billingAddress(billingAddress)
                .type(type)
                .isDefault(paymentMethodRepository.findByUser(user).isEmpty())
                .build();

        pm = paymentMethodRepository.save(pm);

        notificationService.createNotification(user,
                "New payment method added: " + pm.getMaskedCardNumber(),
                NotificationType.CARD_CHANGE);

        logger.info("Payment method added for user: {}", user.getEmail());
        return pm;
    }

    @Override
    @Transactional
    public PaymentMethod updatePaymentMethod(Long pmId, User user, String cardHolderName,
                                             String expiryDate, String billingAddress) {
        PaymentMethod pm = paymentMethodRepository.findById(pmId)
                .orElseThrow(() -> new ResourceNotFoundException("Payment method not found"));

        if (!pm.getUser().getId().equals(user.getId())) {
            throw new ForbiddenOperationException("Unauthorized access to payment method");
        }

        pm.setCardHolderName(cardHolderName);
        pm.setExpiryDate(expiryDate);
        pm.setBillingAddress(billingAddress);

        notificationService.createNotification(user,
                "Payment method updated: " + pm.getMaskedCardNumber(),
                NotificationType.CARD_CHANGE);

        logger.info("Payment method updated for user: {}", user.getEmail());
        return paymentMethodRepository.save(pm);
    }

    @Override
    @Transactional
    public void deletePaymentMethod(Long pmId, User user) {
        PaymentMethod pm = paymentMethodRepository.findById(pmId)
                .orElseThrow(() -> new ResourceNotFoundException("Payment method not found"));

        if (!pm.getUser().getId().equals(user.getId())) {
            throw new ForbiddenOperationException("Unauthorized access to payment method");
        }

        String masked = pm.getMaskedCardNumber();
        paymentMethodRepository.delete(pm);

        notificationService.createNotification(user,
                "Payment method removed: " + masked,
                NotificationType.CARD_CHANGE);

        logger.info("Payment method deleted for user: {}", user.getEmail());
    }

    @Override
    @Transactional
    public void setDefault(Long pmId, User user) {
        List<PaymentMethod> userMethods = paymentMethodRepository.findByUser(user);
        for (PaymentMethod pm : userMethods) {
            pm.setIsDefault(pm.getId().equals(pmId));
            paymentMethodRepository.save(pm);
        }
        logger.info("Default payment method set for user: {}", user.getEmail());
    }

    @Override
    public PaymentMethod getById(Long id) {
        return paymentMethodRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Payment method not found"));
    }
}
