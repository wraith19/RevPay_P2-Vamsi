package com.rev.app.service;

import com.rev.app.entity.Invoice;
import com.rev.app.entity.InvoiceItem;
import com.rev.app.entity.InvoiceStatus;
import com.rev.app.entity.User;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public interface IInvoiceService {
    Invoice createInvoice(User businessUser, String customerName, String customerEmail,
                          String customerAddress, LocalDate dueDate, String paymentTerms,
                          List<InvoiceItem> items);

    Invoice sendInvoice(Long invoiceId, User businessUser);

    Invoice markAsPaid(Long invoiceId, User businessUser);

    Invoice cancelInvoice(Long invoiceId, User businessUser);

    Invoice getInvoiceById(Long id);

    List<Invoice> getBusinessInvoices(User businessUser);

    List<Invoice> getInvoicesByStatus(User businessUser, InvoiceStatus status);

    BigDecimal getPaidTotal(User businessUser);

    BigDecimal getOutstandingTotal(User businessUser);
}
