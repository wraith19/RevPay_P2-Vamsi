package com.rev.app.service.impl;

import com.rev.app.entity.*;
import com.rev.app.repository.IInvoiceRepository;
import com.rev.app.service.IInvoiceService;
import com.rev.app.service.INotificationService;
import lombok.RequiredArgsConstructor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import com.rev.app.exception.ResourceNotFoundException;
import com.rev.app.exception.ForbiddenOperationException;

@Service
@RequiredArgsConstructor
public class InvoiceServiceImpl implements IInvoiceService {

    private static final Logger logger = LogManager.getLogger(InvoiceServiceImpl.class);

    private final IInvoiceRepository invoiceRepository;
    private final INotificationService notificationService;

    @Override
    @Transactional
    public Invoice createInvoice(User businessUser, String customerName, String customerEmail,
                                 String customerAddress, LocalDate dueDate, String paymentTerms,
                                 List<InvoiceItem> items) {
        if (businessUser.getRole() != Role.BUSINESS) {
            throw new ForbiddenOperationException("Only business users can create invoices");
        }

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
        Invoice invoice = getInvoiceById(invoiceId);
        validateOwnership(invoice, businessUser);

        invoice.setStatus(InvoiceStatus.SENT);
        invoice = invoiceRepository.save(invoice);

        notificationService.createNotification(businessUser,
                "Invoice " + invoice.getInvoiceNumber() + " sent to " + invoice.getCustomerName(),
                NotificationType.INVOICE);

        logger.info("Invoice {} sent", invoice.getInvoiceNumber());
        return invoice;
    }

    @Override
    @Transactional
    public Invoice markAsPaid(Long invoiceId, User businessUser) {
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
        return invoiceRepository.findByBusinessUserOrderByCreatedAtDesc(businessUser);
    }

    @Override
    public List<Invoice> getInvoicesByStatus(User businessUser, InvoiceStatus status) {
        return invoiceRepository.findByBusinessUserAndStatus(businessUser, status);
    }

    @Override
    public BigDecimal getPaidTotal(User businessUser) {
        return invoiceRepository.sumPaidInvoiceAmount(businessUser);
    }

    @Override
    public BigDecimal getOutstandingTotal(User businessUser) {
        return invoiceRepository.sumOutstandingInvoiceAmount(businessUser);
    }

    private void validateOwnership(Invoice invoice, User user) {
        if (!invoice.getBusinessUser().getId().equals(user.getId())) {
            throw new ForbiddenOperationException("Unauthorized access to invoice");
        }
    }
}
