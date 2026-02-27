package com.rev.app.service.impl;

import com.rev.app.entity.User;
import com.rev.app.entity.Wallet;
import com.rev.app.repository.IWalletRepository;
import com.rev.app.service.IWalletService;
import lombok.RequiredArgsConstructor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import com.rev.app.exception.ResourceNotFoundException;
import com.rev.app.exception.ValidationException;

@Service
@RequiredArgsConstructor
public class WalletServiceImpl implements IWalletService {

    private static final Logger logger = LogManager.getLogger(WalletServiceImpl.class);
    private static final BigDecimal LOW_BALANCE_THRESHOLD = new BigDecimal("100.00");

    private final IWalletRepository walletRepository;

    @Override
    public Wallet getWalletByUser(User user) {
        return walletRepository.findByUser(user)
                .orElseThrow(() -> new ResourceNotFoundException("Wallet not found for user"));
    }

    @Override
    public BigDecimal getBalance(User user) {
        return getWalletByUser(user).getBalance();
    }

    @Override
    @Transactional
    public Wallet addFunds(User user, BigDecimal amount) {
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new ValidationException("Amount must be positive");
        }

        Wallet wallet = getWalletByUser(user);
        wallet.setBalance(wallet.getBalance().add(amount));
        wallet = walletRepository.save(wallet);

        logger.info("Added {} to wallet of user: {}", amount, user.getEmail());
        return wallet;
    }

    @Override
    @Transactional
    public Wallet withdrawFunds(User user, BigDecimal amount) {
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new ValidationException("Amount must be positive");
        }

        Wallet wallet = getWalletByUser(user);
        if (wallet.getBalance().compareTo(amount) < 0) {
            throw new ValidationException("Insufficient balance");
        }

        wallet.setBalance(wallet.getBalance().subtract(amount));
        wallet = walletRepository.save(wallet);

        logger.info("Withdrawn {} from wallet of user: {}", amount, user.getEmail());
        return wallet;
    }

    @Override
    @Transactional
    public void credit(User user, BigDecimal amount) {
        Wallet wallet = getWalletByUser(user);
        wallet.setBalance(wallet.getBalance().add(amount));
        walletRepository.save(wallet);
    }

    @Override
    @Transactional
    public void debit(User user, BigDecimal amount) {
        Wallet wallet = getWalletByUser(user);
        if (wallet.getBalance().compareTo(amount) < 0) {
            throw new ValidationException("Insufficient balance");
        }
        wallet.setBalance(wallet.getBalance().subtract(amount));
        walletRepository.save(wallet);
    }

    @Override
    public boolean hasSufficientFunds(User user, BigDecimal amount) {
        Wallet wallet = getWalletByUser(user);
        return wallet.getBalance().compareTo(amount) >= 0;
    }

    @Override
    public boolean isLowBalance(User user) {
        return getBalance(user).compareTo(LOW_BALANCE_THRESHOLD) < 0;
    }
}
