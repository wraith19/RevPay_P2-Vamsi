package com.rev.app.service;

import com.rev.app.entity.*;
import com.rev.app.repository.IWalletRepository;
import com.rev.app.service.impl.WalletServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WalletServiceTest {

    @Mock
    private IWalletRepository walletRepository;
    @InjectMocks
    private WalletServiceImpl walletService;

    private User testUser;
    private Wallet testWallet;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .id(1L).email("john@example.com").fullName("John Doe").build();
        testWallet = Wallet.builder()
                .id(1L).user(testUser).balance(new BigDecimal("500.00")).build();
    }

    @Test
    void testGetBalance() {
        when(walletRepository.findByUser(testUser)).thenReturn(Optional.of(testWallet));

        BigDecimal balance = walletService.getBalance(testUser);
        assertEquals(new BigDecimal("500.00"), balance);
    }

    @Test
    void testAddFunds_Success() {
        when(walletRepository.findByUser(testUser)).thenReturn(Optional.of(testWallet));
        when(walletRepository.save(any(Wallet.class))).thenReturn(testWallet);

        Wallet result = walletService.addFunds(testUser, new BigDecimal("100.00"));
        assertNotNull(result);
        assertEquals(new BigDecimal("600.00"), testWallet.getBalance());
    }

    @Test
    void testAddFunds_NegativeAmount() {
        assertThrows(RuntimeException.class, () -> walletService.addFunds(testUser, new BigDecimal("-50.00")));
    }

    @Test
    void testWithdrawFunds_Success() {
        when(walletRepository.findByUser(testUser)).thenReturn(Optional.of(testWallet));
        when(walletRepository.save(any(Wallet.class))).thenReturn(testWallet);

        Wallet result = walletService.withdrawFunds(testUser, new BigDecimal("200.00"));
        assertNotNull(result);
        assertEquals(new BigDecimal("300.00"), testWallet.getBalance());
    }

    @Test
    void testWithdrawFunds_InsufficientBalance() {
        when(walletRepository.findByUser(testUser)).thenReturn(Optional.of(testWallet));

        assertThrows(RuntimeException.class, () ->
                walletService.withdrawFunds(testUser, new BigDecimal("1000.00")));
    }

    @Test
    void testHasSufficientFunds_True() {
        when(walletRepository.findByUser(testUser)).thenReturn(Optional.of(testWallet));
        assertTrue(walletService.hasSufficientFunds(testUser, new BigDecimal("500.00")));
    }

    @Test
    void testHasSufficientFunds_False() {
        when(walletRepository.findByUser(testUser)).thenReturn(Optional.of(testWallet));
        assertFalse(walletService.hasSufficientFunds(testUser, new BigDecimal("600.00")));
    }

    @Test
    void testIsLowBalance() {
        testWallet.setBalance(new BigDecimal("50.00"));
        when(walletRepository.findByUser(testUser)).thenReturn(Optional.of(testWallet));
        assertTrue(walletService.isLowBalance(testUser));
    }
}

