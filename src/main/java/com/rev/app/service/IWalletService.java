package com.rev.app.service;

import com.rev.app.entity.User;
import com.rev.app.entity.Wallet;

import java.math.BigDecimal;

public interface IWalletService {
    Wallet getWalletByUser(User user);

    BigDecimal getBalance(User user);

    Wallet addFunds(User user, BigDecimal amount);

    Wallet withdrawFunds(User user, BigDecimal amount);

    void credit(User user, BigDecimal amount);

    void debit(User user, BigDecimal amount);

    boolean hasSufficientFunds(User user, BigDecimal amount);

    boolean isLowBalance(User user);
}
