package com.rev.app.service;

import com.rev.app.entity.Invoice;
import com.rev.app.entity.InvoiceItem;
import com.rev.app.entity.InvoiceStatus;
import com.rev.app.entity.Role;
import com.rev.app.entity.User;
import com.rev.app.exception.ForbiddenOperationException;
import com.rev.app.repository.IInvoiceRepository;
import com.rev.app.repository.ITransactionRepository;
import com.rev.app.service.impl.InvoiceServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class InvoiceServiceTest {

    @Mock
    private IInvoiceRepository invoiceRepository;
    @Mock
    private ITransactionRepository transactionRepository;
    @Mock
    private INotificationService notificationService;
    @Mock
    private IWalletService walletService;
    @Mock
    private IUserService userService;
    @Mock
    private IPaymentMethodService paymentMethodService;

    @InjectMocks
    private InvoiceServiceImpl invoiceService;

    @Test
    void createInvoice_calculatesTotalsForVerifiedBusinessUser() {
        User business = User.builder()
                .id(21L)
                .email("biz@revpay.com")
                .role(Role.BUSINESS)
                .businessVerified(true)
                .build();

        List<InvoiceItem> items = List.of(
                InvoiceItem.builder().description("Service A").quantity(2).unitPrice(new BigDecimal("50.00")).taxRate(new BigDecimal("10.00")).build(),
                InvoiceItem.builder().description("Service B").quantity(1).unitPrice(new BigDecimal("100.00")).taxRate(new BigDecimal("5.00")).build());

        when(invoiceRepository.save(any(Invoice.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Invoice invoice = invoiceService.createInvoice(
                business,
                "Customer A",
                "customer@revpay.com",
                "Address",
                LocalDate.now().plusDays(7),
                "Net 7",
                items);

        assertEquals(new BigDecimal("200.00"), invoice.getSubtotal());
        assertEquals(new BigDecimal("15.00"), invoice.getTaxAmount());
        assertEquals(new BigDecimal("215.00"), invoice.getTotalAmount());
    }

    @Test
    void getBusinessInvoices_blocksUnverifiedBusinessUser() {
        User unverifiedBusiness = User.builder()
                .id(22L)
                .email("pending@revpay.com")
                .role(Role.BUSINESS)
                .businessVerified(false)
                .build();

        assertThrows(ForbiddenOperationException.class, () -> invoiceService.getBusinessInvoices(unverifiedBusiness));
    }
}
