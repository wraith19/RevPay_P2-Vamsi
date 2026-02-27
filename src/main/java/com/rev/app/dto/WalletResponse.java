package com.rev.app.dto;

import java.math.BigDecimal;

public record WalletResponse(
        Long id,
        Long userId,
        BigDecimal balance,
        boolean lowBalance) {
}
