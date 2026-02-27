package com.rev.app.service;

import com.rev.app.entity.PaymentMethod;
import com.rev.app.entity.PaymentMethodType;
import com.rev.app.entity.User;

import java.util.List;

public interface IPaymentMethodService {
    List<PaymentMethod> getUserPaymentMethods(User user);

    PaymentMethod addPaymentMethod(User user, String cardNumber, String cardHolderName,
                                   String expiryDate, String cvv, String billingAddress,
                                   PaymentMethodType type);

    PaymentMethod updatePaymentMethod(Long pmId, User user, String cardHolderName,
                                      String expiryDate, String billingAddress);

    void deletePaymentMethod(Long pmId, User user);

    void setDefault(Long pmId, User user);

    PaymentMethod getById(Long id);
}
