package com.rev.app.service;

import com.rev.app.entity.*;
import com.rev.app.repository.*;
import com.rev.app.service.impl.UserServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private IUserRepository userRepository;
    @Mock
    private IWalletRepository walletRepository;
    @Mock
    private ISecurityQuestionRepository securityQuestionRepository;
    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserServiceImpl userService;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .id(1L)
                .fullName("John Doe")
                .email("john@example.com")
                .phone("1234567890")
                .password("encoded_password")
                .role(Role.PERSONAL)
                .enabled(true)
                .notifyTransactions(true)
                .notifyRequests(true)
                .notifyCardChanges(true)
                .notifyLowBalance(true)
                .notifyInvoices(true)
                .notifyLoans(true)
                .build();
    }

    @Test
    void testRegisterPersonalUser_Success() {
        when(userRepository.existsByEmail("john@example.com")).thenReturn(false);
        when(userRepository.existsByPhone("1234567890")).thenReturn(false);
        when(passwordEncoder.encode("password")).thenReturn("encoded_password");
        when(userRepository.save(any(User.class))).thenReturn(testUser);
        when(walletRepository.save(any(Wallet.class))).thenReturn(new Wallet());

        User result = userService.registerPersonalUser("John Doe", "john@example.com",
                "1234567890", "password", null);

        assertNotNull(result);
        assertEquals("John Doe", result.getFullName());
        assertEquals("john@example.com", result.getEmail());
        verify(userRepository).save(any(User.class));
        verify(walletRepository).save(any(Wallet.class));
    }

    @Test
    void testRegisterPersonalUser_DuplicateEmail() {
        when(userRepository.existsByEmail("john@example.com")).thenReturn(true);

        assertThrows(RuntimeException.class, () ->
                userService.registerPersonalUser("John Doe", "john@example.com",
                        "1234567890", "password", null));
    }

    @Test
    void testRegisterPersonalUser_DuplicatePhone() {
        when(userRepository.existsByEmail("john@example.com")).thenReturn(false);
        when(userRepository.existsByPhone("1234567890")).thenReturn(true);

        assertThrows(RuntimeException.class, () ->
                userService.registerPersonalUser("John Doe", "john@example.com",
                        "1234567890", "password", null));
    }

    @Test
    void testFindByEmail() {
        when(userRepository.findByEmail("john@example.com")).thenReturn(Optional.of(testUser));

        Optional<User> result = userService.findByEmail("john@example.com");

        assertTrue(result.isPresent());
        assertEquals("john@example.com", result.get().getEmail());
    }

    @Test
    void testChangePassword_Success() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches("old_pass", "encoded_password")).thenReturn(true);
        when(passwordEncoder.encode("new_pass")).thenReturn("new_encoded");
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        assertDoesNotThrow(() ->
                userService.changePassword(1L, "old_pass", "new_pass"));
        verify(userRepository).save(any(User.class));
    }

    @Test
    void testChangePassword_WrongCurrentPassword() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches("wrong_pass", "encoded_password")).thenReturn(false);

        assertThrows(RuntimeException.class, () ->
                userService.changePassword(1L, "wrong_pass", "new_pass"));
    }

    @Test
    void testSetTransactionPin() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(passwordEncoder.encode("1234")).thenReturn("encoded_pin");
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        assertDoesNotThrow(() -> userService.setTransactionPin(1L, "1234"));
        verify(userRepository).save(any(User.class));
    }
}

