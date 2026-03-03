package com.rev.app.rest;

import com.rev.app.entity.Loan;
import com.rev.app.entity.LoanStatus;
import com.rev.app.entity.Role;
import com.rev.app.entity.User;
import com.rev.app.exception.ApiExceptionHandler;
import com.rev.app.service.ILoanService;
import com.rev.app.service.IUserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = LoanRestController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(ApiExceptionHandler.class)
class LoanRestControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ILoanService loanService;

    @MockBean
    private IUserService userService;

    @Test
    void getMyLoans_returnsLoansForBusinessUser() throws Exception {
        User businessUser = User.builder()
                .id(10L)
                .email("business@revpay.com")
                .role(Role.BUSINESS)
                .build();
        Loan loan = Loan.builder()
                .id(100L)
                .businessUser(businessUser)
                .amount(new BigDecimal("12000.00"))
                .purpose("Working capital")
                .tenureMonths(12)
                .interestRate(new BigDecimal("12.0"))
                .status(LoanStatus.PENDING)
                .build();

        when(userService.findByEmail("business@revpay.com")).thenReturn(Optional.of(businessUser));
        when(loanService.getUserLoans(businessUser)).thenReturn(List.of(loan));

        mockMvc.perform(get("/api/loans/my")
                        .principal(() -> "business@revpay.com")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(100))
                .andExpect(jsonPath("$[0].purpose").value("Working capital"))
                .andExpect(jsonPath("$[0].status").value("PENDING"));
    }

    @Test
    void getMyLoans_returnsForbiddenForAdminUser() throws Exception {
        User adminUser = User.builder()
                .id(1L)
                .email("admin@revpay.com")
                .role(Role.ADMIN)
                .build();

        when(userService.findByEmail("admin@revpay.com")).thenReturn(Optional.of(adminUser));

        mockMvc.perform(get("/api/loans/my")
                        .principal(() -> "admin@revpay.com")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").value("This endpoint is available only for BUSINESS users"))
                .andExpect(jsonPath("$.status").value(403));
    }
}
