package com.rev.app.rest;

import com.rev.app.entity.Role;
import com.rev.app.entity.User;
import com.rev.app.exception.ApiExceptionHandler;
import com.rev.app.service.ILoanService;
import com.rev.app.service.IUserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = LoanRestController.class)
@AutoConfigureMockMvc(addFilters = true)
@Import(ApiExceptionHandler.class)
class LoanRestControllerSecurityTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ILoanService loanService;

    @MockBean
    private IUserService userService;

    @Test
    void unauthenticatedUser_cannotAccessProtectedLoanApi() throws Exception {
        mockMvc.perform(get("/api/loans/my"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(username = "business@revpay.com", roles = "BUSINESS")
    void businessUser_cannotAccessAdminOnlyPendingLoansApi() throws Exception {
        User businessUser = User.builder()
                .id(2L)
                .email("business@revpay.com")
                .role(Role.BUSINESS)
                .build();
        when(userService.findByEmail("business@revpay.com")).thenReturn(Optional.of(businessUser));

        mockMvc.perform(get("/api/loans/pending"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "admin@revpay.com", roles = "ADMIN")
    void adminUser_canAccessPendingLoansApi() throws Exception {
        User adminUser = User.builder()
                .id(1L)
                .email("admin@revpay.com")
                .role(Role.ADMIN)
                .build();

        when(userService.findByEmail("admin@revpay.com")).thenReturn(Optional.of(adminUser));
        when(loanService.getPendingLoans()).thenReturn(List.of());

        mockMvc.perform(get("/api/loans/pending"))
                .andExpect(status().isOk());

        verify(loanService).getPendingLoans();
    }
}
