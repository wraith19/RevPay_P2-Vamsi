package com.rev.app.service.impl;

import com.rev.app.entity.*;
import com.rev.app.exception.ForbiddenOperationException;
import com.rev.app.exception.ResourceNotFoundException;
import com.rev.app.exception.ValidationException;
import com.rev.app.repository.IInvoiceRepository;
import com.rev.app.repository.ITransactionRepository;
import com.rev.app.service.IInvoiceService;
import com.rev.app.service.INotificationService;
import com.rev.app.service.IPaymentMethodService;
import com.rev.app.service.IUserService;
import com.rev.app.service.IWalletService;
import lombok.RequiredArgsConstructor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class InvoiceServiceImpl implements IInvoiceService {

    private static final Logger logger = LogManager.getLogger(InvoiceServiceImpl.class);

    private final IInvoiceRepository invoiceRepository;
    private final ITransactionRepository transactionRepository;
    private final INotificationService notificationService;
    private final IWalletService walletService;
    private final IUserService userService;
    private final IPaymentMethodService paymentMethodService;

    @Override
    @Transactional
    public Invoice createInvoice(User businessUser, String customerName, String customerEmail,
                                 String customerAddress, LocalDate dueDate, String paymentTerms,
                                 List<InvoiceItem> items) {
        if (businessUser.getRole() != Role.BUSINESS) {
            throw new ForbiddenOperationException("Only business users can create invoices");
        }
        ensureBusinessUserVerified(businessUser);

        Invoice invoice = Invoice.builder()
                .businessUser(businessUser)
                .customerName(customerName)
                .customerEmail(customerEmail)
                .customerAddress(customerAddress)
                .dueDate(dueDate)
                .paymentTerms(paymentTerms)
                .status(InvoiceStatus.DRAFT)
                .build();

        if (items != null) {
            for (InvoiceItem item : items) {
                item.setInvoice(invoice);
                invoice.getItems().add(item);
            }
        }

        invoice.calculateTotals();
        invoice = invoiceRepository.save(invoice);

        logger.info("Invoice created: {} by user: {}", invoice.getInvoiceNumber(), businessUser.getEmail());
        return invoice;
    }

    @Override
    @Transactional
    public Invoice sendInvoice(Long invoiceId, User businessUser) {
        ensureBusinessUserVerified(businessUser);
        Invoice invoice = getInvoiceById(invoiceId);
        validateOwnership(invoice, businessUser);

        invoice.setStatus(InvoiceStatus.SENT);
        invoice = invoiceRepository.save(invoice);
        Invoice sentInvoice = invoice;

        notificationService.createNotification(businessUser,
                "Invoice " + invoice.getInvoiceNumber() + " sent to " + invoice.getCustomerName(),
                NotificationType.INVOICE);

        userService.findByEmail(sentInvoice.getCustomerEmail()).ifPresent(customerUser ->
                notificationService.createNotification(customerUser,
                        "New invoice " + sentInvoice.getInvoiceNumber() + " from " + businessUser.getFullName()
                                + " for $" + sentInvoice.getTotalAmount(),
                        NotificationType.INVOICE));

        logger.info("Invoice {} sent", invoice.getInvoiceNumber());
        return invoice;
    }

    @Override
    @Transactional
    public Invoice markAsPaid(Long invoiceId, User businessUser) {
        ensureBusinessUserVerified(businessUser);
        Invoice invoice = getInvoiceById(invoiceId);
        validateOwnership(invoice, businessUser);

        invoice.setStatus(InvoiceStatus.PAID);
        invoice = invoiceRepository.save(invoice);

        notificationService.createNotification(businessUser,
                "Invoice " + invoice.getInvoiceNumber() + " marked as paid ($" + invoice.getTotalAmount() + ")",
                NotificationType.INVOICE);

        logger.info("Invoice {} marked as paid", invoice.getInvoiceNumber());
        return invoice;
    }

    @Override
    @Transactional
    public Invoice cancelInvoice(Long invoiceId, User businessUser) {
        ensureBusinessUserVerified(businessUser);
        Invoice invoice = getInvoiceById(invoiceId);
        validateOwnership(invoice, businessUser);

        invoice.setStatus(InvoiceStatus.CANCELLED);
        invoice = invoiceRepository.save(invoice);

        logger.info("Invoice {} cancelled", invoice.getInvoiceNumber());
        return invoice;
    }

    @Override
    public Invoice getInvoiceById(Long id) {
        return invoiceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Invoice not found"));
    }

    @Override
    public List<Invoice> getBusinessInvoices(User businessUser) {
        ensureBusinessUserVerified(businessUser);
        return invoiceRepository.findByBusinessUserOrderByCreatedAtDesc(businessUser);
    }

    @Override
    public List<Invoice> getInvoicesByStatus(User businessUser, InvoiceStatus status) {
        ensureBusinessUserVerified(businessUser);
        return invoiceRepository.findByBusinessUserAndStatus(businessUser, status);
    }

    @Override
    public BigDecimal getPaidTotal(User businessUser) {
        ensureBusinessUserVerified(businessUser);
        return invoiceRepository.sumPaidInvoiceAmount(businessUser);
    }

    @Override
    public BigDecimal getOutstandingTotal(User businessUser) {
        ensureBusinessUserVerified(businessUser);
        return invoiceRepository.sumOutstandingInvoiceAmount(businessUser);
    }

    @Override
    public List<Invoice> getReceivedInvoices(User user) {
        return invoiceRepository.findByCustomerEmailOrderByCreatedAtDesc(user.getEmail());
    }

    @Override
    @Transactional
    public Invoice payInvoice(Long invoiceId, User payingUser, String paymentSource, Long paymentMethodId) {
        ensureBusinessUserVerified(payingUser);
        Invoice invoice = getInvoiceById(invoiceId);

        if (invoice.getStatus() != InvoiceStatus.SENT) {
            throw new ForbiddenOperationException("Only SENT invoices can be paid");
        }

        if (invoice.getCustomerEmail() == null ||
                !invoice.getCustomerEmail().equalsIgnoreCase(payingUser.getEmail())) {
            throw new ForbiddenOperationException("This invoice is not addressed to you");
        }

        BigDecimal amount = invoice.getTotalAmount();
        String source = paymentSource == null ? "WALLET" : paymentSource.trim().toUpperCase();
        String sourceNote;

        if ("WALLET".equals(source)) {
            if (!walletService.hasSufficientFunds(payingUser, amount)) {
                throw new ValidationException("Insufficient wallet balance to pay this invoice");
            }
            walletService.debit(payingUser, amount);
            sourceNote = "wallet";
        } else if ("CARD".equals(source)) {
            if (paymentMethodId == null) {
                throw new ValidationException("Select a saved card to pay this invoice");
            }

            PaymentMethod paymentMethod = paymentMethodService.getById(paymentMethodId);
            if (!paymentMethod.getUser().getId().equals(payingUser.getId())) {
                throw new ForbiddenOperationException("Unauthorized payment method");
            }

            if (paymentMethod.getType() != PaymentMethodType.CREDIT_CARD
                    && paymentMethod.getType() != PaymentMethodType.DEBIT_CARD) {
                throw new ValidationException("Only saved cards can be used for this payment");
            }
            sourceNote = paymentMethod.getMaskedCardNumber();
        } else {
            throw new ValidationException("Unsupported payment source");
        }

        walletService.credit(invoice.getBusinessUser(), amount);

        Transaction paymentTransaction = Transaction.builder()
                .sender(payingUser)
                .receiver(invoice.getBusinessUser())
                .amount(amount)
                .type(TransactionType.SEND)
                .status(TransactionStatus.SUCCESS)
                .note("Invoice " + invoice.getInvoiceNumber() + " paid via " + sourceNote)
                .build();
        transactionRepository.save(paymentTransaction);

        invoice.setStatus(InvoiceStatus.PAID);
        invoice = invoiceRepository.save(invoice);

        notificationService.createNotification(payingUser,
                "Invoice " + invoice.getInvoiceNumber() + " paid - $" + amount,
                NotificationType.INVOICE);

        notificationService.createNotification(invoice.getBusinessUser(),
                "Payment received for " + invoice.getInvoiceNumber() + " - $" + amount,
                NotificationType.INVOICE);

        logger.info("Invoice {} paid by {}", invoice.getInvoiceNumber(), payingUser.getEmail());
        return invoice;
    }

    private void validateOwnership(Invoice invoice, User user) {
        if (!invoice.getBusinessUser().getId().equals(user.getId())) {
            throw new ForbiddenOperationException("Unauthorized access to invoice");
        }
    }

    private void ensureBusinessUserVerified(User user) {
        if (user != null && user.getRole() == Role.BUSINESS && (user.getBusinessVerified() == null || !user.getBusinessVerified())) {
            throw new ForbiddenOperationException("Business account is pending verification by admin");
        }
    }
}
