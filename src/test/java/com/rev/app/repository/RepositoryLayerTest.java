package com.rev.app.repository;

import com.rev.app.entity.MoneyRequest;
import com.rev.app.entity.RequestStatus;
import com.rev.app.entity.Role;
import com.rev.app.entity.Transaction;
import com.rev.app.entity.TransactionStatus;
import com.rev.app.entity.TransactionType;
import com.rev.app.entity.Invoice;
import com.rev.app.entity.InvoiceStatus;
import com.rev.app.entity.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class RepositoryLayerTest {

    @Autowired
    private IUserRepository userRepository;

    @Autowired
    private IMoneyRequestRepository moneyRequestRepository;

    @Autowired
    private ITransactionRepository transactionRepository;

    @Autowired
    private IInvoiceRepository invoiceRepository;

    @Test
    void userRepository_findByEmailOrPhone_returnsMatchingUser() {
        User saved = userRepository.save(buildUser("biz@revpay.com", "+15550000001", Role.BUSINESS, false));

        Optional<User> byEmail = userRepository.findByEmailOrPhone(saved.getEmail(), saved.getEmail());
        Optional<User> byPhone = userRepository.findByEmailOrPhone(saved.getPhone(), saved.getPhone());

        assertTrue(byEmail.isPresent());
        assertTrue(byPhone.isPresent());
        assertEquals(saved.getId(), byEmail.get().getId());
        assertEquals(saved.getId(), byPhone.get().getId());
    }

    @Test
    void userRepository_countByRoleAndBusinessVerified_countsPendingBusinessUsers() {
        userRepository.save(buildUser("pending1@revpay.com", "+15550000011", Role.BUSINESS, false));
        userRepository.save(buildUser("pending2@revpay.com", "+15550000012", Role.BUSINESS, false));
        userRepository.save(buildUser("verified@revpay.com", "+15550000013", Role.BUSINESS, true));
        userRepository.save(buildUser("personal@revpay.com", "+15550000014", Role.PERSONAL, false));

        long pendingCount = userRepository.countByRoleAndBusinessVerified(Role.BUSINESS, false);

        assertEquals(2L, pendingCount);
    }

    @Test
    void moneyRequestRepository_findByRequesteeAndStatus_returnsOnlyPendingForRequestee() {
        User requester = userRepository.save(buildUser("requester@revpay.com", "+15550000021", Role.PERSONAL, false));
        User requestee = userRepository.save(buildUser("requestee@revpay.com", "+15550000022", Role.PERSONAL, false));
        User otherRequestee = userRepository.save(buildUser("other@revpay.com", "+15550000023", Role.PERSONAL, false));

        moneyRequestRepository.save(MoneyRequest.builder()
                .requester(requester)
                .requestee(requestee)
                .amount(new BigDecimal("25.00"))
                .purpose("Lunch")
                .status(RequestStatus.PENDING)
                .build());
        moneyRequestRepository.save(MoneyRequest.builder()
                .requester(requester)
                .requestee(requestee)
                .amount(new BigDecimal("60.00"))
                .purpose("Bills")
                .status(RequestStatus.ACCEPTED)
                .build());
        moneyRequestRepository.save(MoneyRequest.builder()
                .requester(requester)
                .requestee(otherRequestee)
                .amount(new BigDecimal("35.00"))
                .purpose("Taxi")
                .status(RequestStatus.PENDING)
                .build());

        List<MoneyRequest> pending = moneyRequestRepository.findByRequesteeAndStatus(requestee, RequestStatus.PENDING);

        assertEquals(1, pending.size());
        assertEquals(RequestStatus.PENDING, pending.get(0).getStatus());
        assertEquals(requestee.getId(), pending.get(0).getRequestee().getId());
    }

    @Test
    void transactionRepository_sumReceivedAndSentAmount_countsOnlySuccessfulTransactions() {
        User sender = userRepository.save(buildUser("sender@revpay.com", "+15550000031", Role.PERSONAL, false));
        User receiver = userRepository.save(buildUser("receiver@revpay.com", "+15550000032", Role.PERSONAL, false));

        transactionRepository.save(Transaction.builder()
                .sender(sender)
                .receiver(receiver)
                .amount(new BigDecimal("75.00"))
                .type(TransactionType.SEND)
                .status(TransactionStatus.SUCCESS)
                .build());
        transactionRepository.save(Transaction.builder()
                .sender(sender)
                .receiver(receiver)
                .amount(new BigDecimal("20.00"))
                .type(TransactionType.SEND)
                .status(TransactionStatus.PENDING)
                .build());

        BigDecimal sent = transactionRepository.sumSentAmount(sender);
        BigDecimal received = transactionRepository.sumReceivedAmount(receiver);

        assertEquals(new BigDecimal("75.00"), sent);
        assertEquals(new BigDecimal("75.00"), received);
    }

    @Test
    void invoiceRepository_sumOutstandingInvoiceAmount_includesSentAndOverdueOnly() {
        User business = userRepository.save(buildUser("inv-biz@revpay.com", "+15550000041", Role.BUSINESS, true));

        invoiceRepository.save(Invoice.builder()
                .businessUser(business)
                .customerName("A")
                .status(InvoiceStatus.SENT)
                .totalAmount(new BigDecimal("90.00"))
                .build());
        invoiceRepository.save(Invoice.builder()
                .businessUser(business)
                .customerName("B")
                .status(InvoiceStatus.OVERDUE)
                .totalAmount(new BigDecimal("60.00"))
                .build());
        invoiceRepository.save(Invoice.builder()
                .businessUser(business)
                .customerName("C")
                .status(InvoiceStatus.PAID)
                .totalAmount(new BigDecimal("40.00"))
                .build());

        BigDecimal outstanding = invoiceRepository.sumOutstandingInvoiceAmount(business);

        assertEquals(new BigDecimal("150.00"), outstanding);
    }

    private User buildUser(String email, String phone, Role role, boolean businessVerified) {
        return User.builder()
                .fullName(email.split("@")[0])
                .email(email)
                .phone(phone)
                .password("encoded")
                .role(role)
                .enabled(true)
                .businessVerified(businessVerified)
                .build();
    }
}
