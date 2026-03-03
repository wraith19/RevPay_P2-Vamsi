package com.rev.app.service.impl;

import com.rev.app.entity.*;
import com.rev.app.repository.ISecurityQuestionRepository;
import com.rev.app.repository.IUserRepository;
import com.rev.app.repository.IWalletRepository;
import com.rev.app.service.IUserService;
import lombok.RequiredArgsConstructor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import com.rev.app.exception.ResourceNotFoundException;
import com.rev.app.exception.ConflictException;
import com.rev.app.exception.ValidationException;
import com.rev.app.exception.ForbiddenOperationException;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements IUserService {

    private static final Logger logger = LogManager.getLogger(UserServiceImpl.class);

    private final IUserRepository userRepository;
    private final IWalletRepository walletRepository;
    private final ISecurityQuestionRepository securityQuestionRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public User registerPersonalUser(String fullName, String email, String phone, String password,
                                     List<SecurityQuestion> questions) {
        logger.info("Registering personal user: {}", email);

        if (userRepository.existsByEmail(email)) {
            throw new ConflictException("Email already registered");
        }
        if (userRepository.existsByPhone(phone)) {
            throw new ConflictException("Phone number already registered");
        }

        User user = User.builder()
                .fullName(fullName)
                .email(email)
                .phone(phone)
                .password(passwordEncoder.encode(password))
                .role(Role.PERSONAL)
                .build();

        user = userRepository.save(user);

        Wallet wallet = Wallet.builder()
                .user(user)
                .balance(BigDecimal.ZERO)
                .build();
        walletRepository.save(wallet);

        if (questions != null) {
            for (SecurityQuestion q : questions) {
                q.setUser(user);
                q.setAnswer(passwordEncoder.encode(q.getAnswer()));
                securityQuestionRepository.save(q);
            }
        }

        logger.info("Personal user registered successfully: {}", email);
        return user;
    }

    @Override
    @Transactional
    public User registerBusinessUser(String fullName, String email, String phone, String password,
                                     String businessName, String businessType, String taxId,
                                     String businessAddress, String contactInfo,
                                     List<SecurityQuestion> questions) {
        logger.info("Registering business user: {}", email);

        if (userRepository.existsByEmail(email)) {
            throw new ConflictException("Email already registered");
        }
        if (userRepository.existsByPhone(phone)) {
            throw new ConflictException("Phone number already registered");
        }

        User user = User.builder()
                .fullName(fullName)
                .email(email)
                .phone(phone)
                .password(passwordEncoder.encode(password))
                .role(Role.BUSINESS)
                .businessName(businessName)
                .businessType(businessType)
                .taxId(taxId)
                .businessAddress(businessAddress)
                .businessContactInfo(contactInfo)
                .businessVerified(false)
                .build();

        user = userRepository.save(user);

        Wallet wallet = Wallet.builder()
                .user(user)
                .balance(BigDecimal.ZERO)
                .build();
        walletRepository.save(wallet);

        if (questions != null) {
            for (SecurityQuestion q : questions) {
                q.setUser(user);
                q.setAnswer(passwordEncoder.encode(q.getAnswer()));
                securityQuestionRepository.save(q);
            }
        }

        logger.info("Business user registered successfully: {}", email);
        return user;
    }

    @Override
    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    @Override
    public Optional<User> findByPhone(String phone) {
        return userRepository.findByPhone(phone);
    }

    @Override
    public Optional<User> findById(Long id) {
        return userRepository.findById(id);
    }

    @Override
    public Optional<User> findByEmailOrPhone(String identifier) {
        return userRepository.findByEmailOrPhone(identifier, identifier);
    }

    @Override
    public List<User> getAllUsers() {
        return userRepository.findAllByOrderByCreatedAtDesc();
    }

    @Override
    public List<User> getUsersByRole(Role role) {
        return userRepository.findByRoleOrderByCreatedAtDesc(role);
    }

    @Override
    public List<User> getPendingBusinessVerifications() {
        return userRepository.findByRoleAndBusinessVerifiedOrderByCreatedAtDesc(Role.BUSINESS, false);
    }

    @Override
    @Transactional
    public User updateProfile(Long userId, String fullName, String phone, String businessName,
                              String businessType, String businessAddress, String contactInfo) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        user.setFullName(fullName);
        if (phone != null && !phone.equals(user.getPhone())) {
            if (userRepository.existsByPhone(phone)) {
                throw new ConflictException("Phone number already in use");
            }
            user.setPhone(phone);
        }

        if (user.getRole() == Role.BUSINESS) {
            user.setBusinessName(businessName);
            user.setBusinessType(businessType);
            user.setBusinessAddress(businessAddress);
            user.setBusinessContactInfo(contactInfo);
        }

        logger.info("Profile updated for user: {}", user.getEmail());
        return userRepository.save(user);
    }

    @Override
    @Transactional
    public void changePassword(Long userId, String currentPassword, String newPassword) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (!passwordEncoder.matches(currentPassword, user.getPassword())) {
            throw new ValidationException("Current password is incorrect");
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
        logger.info("Password changed for user: {}", user.getEmail());
    }

    @Override
    public List<SecurityQuestion> getSecurityQuestionsForUser(String identifier) {
        User user = userRepository.findByEmailOrPhone(identifier, identifier)
                .orElseThrow(() -> new ResourceNotFoundException("Account not found"));
        return securityQuestionRepository.findByUserId(user.getId());
    }

    @Override
    @Transactional
    public void validateSecurityQuestionAnswer(String identifier, Long questionId, String answer) {
        if (answer == null || answer.isBlank()) {
            throw new ValidationException("Security answer is required");
        }

        User user = userRepository.findByEmailOrPhone(identifier, identifier)
                .orElseThrow(() -> new ResourceNotFoundException("Account not found"));
        SecurityQuestion securityQuestion = securityQuestionRepository.findById(questionId)
                .orElseThrow(() -> new ResourceNotFoundException("Security question not found"));

        if (!securityQuestion.getUser().getId().equals(user.getId())) {
            throw new ForbiddenOperationException("Invalid security question selection");
        }
        if (!passwordEncoder.matches(answer.trim(), securityQuestion.getAnswer())) {
            throw new ValidationException("Security answer is incorrect");
        }
    }

    @Override
    @Transactional
    public void resetPasswordWithoutCurrent(String identifier, String newPassword) {
        if (newPassword == null || newPassword.length() < 6) {
            throw new ValidationException("Password must be at least 6 characters");
        }

        User user = userRepository.findByEmailOrPhone(identifier, identifier)
                .orElseThrow(() -> new ResourceNotFoundException("Account not found"));

        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
        logger.info("Password reset completed for user: {}", user.getEmail());
    }

    @Override
    @Transactional
    public void setTransactionPin(Long userId, String pin) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        user.setTransactionPin(passwordEncoder.encode(pin));
        userRepository.save(user);
        logger.info("Transaction PIN set for user: {}", user.getEmail());
    }

    @Override
    public boolean verifyTransactionPin(Long userId, String pin) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (user.getTransactionPin() == null) {
            return true;
        }
        return passwordEncoder.matches(pin, user.getTransactionPin());
    }

    @Override
    @Transactional
    public void updateNotificationPreferences(Long userId, Boolean transactions, Boolean requests,
                                              Boolean cardChanges, Boolean lowBalance,
                                              Boolean invoices, Boolean loans) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        user.setNotifyTransactions(transactions);
        user.setNotifyRequests(requests);
        user.setNotifyCardChanges(cardChanges);
        user.setNotifyLowBalance(lowBalance);
        user.setNotifyInvoices(invoices);
        user.setNotifyLoans(loans);

        userRepository.save(user);
        logger.info("Notification preferences updated for user: {}", user.getEmail());
    }

    @Override
    @Transactional
    public void verifyBusiness(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        if (user.getRole() != Role.BUSINESS) {
            throw new ForbiddenOperationException("Only business users can be verified");
        }
        user.setBusinessVerified(true);
        userRepository.save(user);
        logger.info("Business verified for user: {}", user.getEmail());
    }

    @Override
    @Transactional
    public void setUserEnabled(Long userId, boolean enabled) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        if (user.getRole() == Role.ADMIN && !enabled) {
            throw new ValidationException("Admin account cannot be disabled");
        }
        user.setEnabled(enabled);
        userRepository.save(user);
        logger.info("Updated enabled status for user {} to {}", user.getEmail(), enabled);
    }

    @Override
    @Transactional
    public User updateUserAsAdmin(Long userId, String fullName, String email, String phone, Role role,
                                  Boolean enabled, String businessName, String businessType, String taxId,
                                  String businessAddress, String businessContactInfo, Boolean businessVerified) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (!user.getEmail().equalsIgnoreCase(email) && userRepository.existsByEmail(email)) {
            throw new ConflictException("Email already in use");
        }
        if (!user.getPhone().equals(phone) && userRepository.existsByPhone(phone)) {
            throw new ConflictException("Phone number already in use");
        }

        user.setFullName(fullName);
        user.setEmail(email);
        user.setPhone(phone);
        user.setRole(role);
        user.setEnabled(enabled != null ? enabled : true);

        if (role == Role.BUSINESS) {
            user.setBusinessName(businessName);
            user.setBusinessType(businessType);
            user.setTaxId(taxId);
            user.setBusinessAddress(businessAddress);
            user.setBusinessContactInfo(businessContactInfo);
            user.setBusinessVerified(businessVerified != null ? businessVerified : false);
        } else {
            user.setBusinessName(null);
            user.setBusinessType(null);
            user.setTaxId(null);
            user.setBusinessAddress(null);
            user.setBusinessContactInfo(null);
            user.setBusinessVerified(false);
        }

        logger.info("Admin updated account {} ({})", user.getId(), user.getEmail());
        return userRepository.save(user);
    }

    @Override
    public long countUsersByRole(Role role) {
        return userRepository.countByRole(role);
    }

    @Override
    public long countPendingBusinessVerifications() {
        return userRepository.countByRoleAndBusinessVerified(Role.BUSINESS, false);
    }
}
