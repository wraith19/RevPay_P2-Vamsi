package com.rev.app.service;

import com.rev.app.entity.Role;
import com.rev.app.entity.SecurityQuestion;
import com.rev.app.entity.User;

import java.util.List;
import java.util.Optional;

public interface IUserService {
    User registerPersonalUser(String fullName, String email, String phone, String password, List<SecurityQuestion> questions);

    User registerBusinessUser(String fullName, String email, String phone, String password,
                              String businessName, String businessType, String taxId,
                              String businessAddress, String contactInfo,
                              List<SecurityQuestion> questions);

    Optional<User> findByEmail(String email);

    Optional<User> findByPhone(String phone);

    Optional<User> findById(Long id);

    Optional<User> findByEmailOrPhone(String identifier);

    List<User> getAllUsers();

    List<User> getUsersByRole(Role role);

    List<User> getPendingBusinessVerifications();

    User updateProfile(Long userId, String fullName, String phone, String businessName,
                       String businessType, String businessAddress, String contactInfo);

    void changePassword(Long userId, String currentPassword, String newPassword);

    void setTransactionPin(Long userId, String pin);

    boolean verifyTransactionPin(Long userId, String pin);

    void updateNotificationPreferences(Long userId, Boolean transactions, Boolean requests,
                                       Boolean cardChanges, Boolean lowBalance,
                                       Boolean invoices, Boolean loans);

    void verifyBusiness(Long userId);

    void setUserEnabled(Long userId, boolean enabled);

    long countUsersByRole(Role role);

    long countPendingBusinessVerifications();
}
