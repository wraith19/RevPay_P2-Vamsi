package com.rev.app.config;

import com.rev.app.entity.Role;
import com.rev.app.entity.User;
import com.rev.app.entity.Wallet;
import com.rev.app.repository.IUserRepository;
import com.rev.app.repository.IWalletRepository;
import lombok.RequiredArgsConstructor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.math.BigDecimal;

@Configuration
@Profile("!test")
@RequiredArgsConstructor
public class AdminAccountInitializer {

    private static final Logger logger = LogManager.getLogger(AdminAccountInitializer.class);

    private final IUserRepository userRepository;
    private final IWalletRepository walletRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.admin.email:admin@revpay.com}")
    private String adminEmail;

    @Value("${app.admin.password:Admin@123}")
    private String adminPassword;

    @Value("${app.admin.full-name:RevPay Admin}")
    private String adminFullName;

    @Value("${app.admin.phone:+10000000000}")
    private String adminPhone;

    @Bean
    public ApplicationRunner seedAdminAccount() {
        return args -> {
            if (userRepository.findByEmail(adminEmail).isPresent()) {
                return;
            }

            String adminPhoneToUse = adminPhone;
            if (userRepository.existsByPhone(adminPhoneToUse)) {
                adminPhoneToUse = "ADMIN-" + System.currentTimeMillis();
                logger.warn("Configured admin phone already exists, using generated phone: {}", adminPhoneToUse);
            }

            User admin = User.builder()
                    .fullName(adminFullName)
                    .email(adminEmail)
                    .phone(adminPhoneToUse)
                    .password(passwordEncoder.encode(adminPassword))
                    .role(Role.ADMIN)
                    .enabled(true)
                    .businessVerified(true)
                    .build();

            admin = userRepository.save(admin);
            walletRepository.save(Wallet.builder()
                    .user(admin)
                    .balance(BigDecimal.ZERO)
                    .build());

            logger.warn("Default admin account created: {}", adminEmail);
        };
    }
}
