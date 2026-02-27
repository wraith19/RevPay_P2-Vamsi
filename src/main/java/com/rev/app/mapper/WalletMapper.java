package com.rev.app.mapper;

import com.rev.app.dto.WalletResponse;
import com.rev.app.entity.Wallet;

import java.math.BigDecimal;

public final class WalletMapper {

    private WalletMapper() {
    }

    public static WalletResponse toResponse(Wallet wallet, BigDecimal lowBalanceThreshold) {
        return new WalletResponse(
                wallet.getId(),
                wallet.getUser() != null ? wallet.getUser().getId() : null,
                wallet.getBalance(),
                wallet.getBalance() != null && wallet.getBalance().compareTo(lowBalanceThreshold) < 0);
    }
}
