package com.rev.app.rest;

import com.rev.app.dto.ApiMessageResponse;
import com.rev.app.dto.CreateInvoiceRequest;
import com.rev.app.dto.InvoiceResponse;
import com.rev.app.entity.Invoice;
import com.rev.app.entity.InvoiceItem;
import com.rev.app.entity.Role;
import com.rev.app.entity.User;
import com.rev.app.mapper.InvoiceMapper;
import com.rev.app.service.IInvoiceService;
import com.rev.app.service.IUserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;
import java.time.LocalDateTime;
import java.util.List;
import com.rev.app.exception.ResourceNotFoundException;
import com.rev.app.exception.ForbiddenOperationException;

@RestController
@RequestMapping("/api/invoices")
@RequiredArgsConstructor
public class InvoiceRestController {

    private final IInvoiceService invoiceService;
    private final IUserService userService;

    @GetMapping("/my")
    public List<InvoiceResponse> getMyInvoices(Principal principal) {
        User user = getAuthenticatedBusinessUser(principal);

        return invoiceService.getBusinessInvoices(user)
                .stream()
                .map(InvoiceMapper::toResponse)
                .toList();
    }

    @GetMapping("/{id}")
    public InvoiceResponse getInvoiceById(@PathVariable Long id, Principal principal) {
        User user = getAuthenticatedBusinessUser(principal);
        Invoice invoice = invoiceService.getInvoiceById(id);
        if (!invoice.getBusinessUser().getId().equals(user.getId())) {
            throw new ForbiddenOperationException("Unauthorized access to invoice");
        }
        return InvoiceMapper.toResponse(invoice);
    }

    @PostMapping
    public InvoiceResponse createInvoice(
            Principal principal,
            @Valid @RequestBody CreateInvoiceRequest request) {
        User user = getAuthenticatedBusinessUser(principal);
        List<InvoiceItem> items = request.itemsOrEmpty().stream()
                .map(i -> InvoiceItem.builder()
                        .description(i.description())
                        .quantity(i.quantity())
                        .unitPrice(i.unitPrice())
                        .taxRate(i.taxRate())
                        .build())
                .toList();

        Invoice created = invoiceService.createInvoice(
                user,
                request.customerName(),
                request.customerEmail(),
                request.customerAddress(),
                request.dueDate(),
                request.paymentTerms(),
                items);
        return InvoiceMapper.toResponse(created);
    }

    @PatchMapping("/{id}/send")
    public InvoiceResponse sendInvoice(@PathVariable Long id, Principal principal) {
        User user = getAuthenticatedBusinessUser(principal);
        return InvoiceMapper.toResponse(invoiceService.sendInvoice(id, user));
    }

    @PatchMapping("/{id}/mark-paid")
    public InvoiceResponse markAsPaid(@PathVariable Long id, Principal principal) {
        User user = getAuthenticatedBusinessUser(principal);
        return InvoiceMapper.toResponse(invoiceService.markAsPaid(id, user));
    }

    @PatchMapping("/{id}/cancel")
    public ApiMessageResponse cancelInvoice(@PathVariable Long id, Principal principal) {
        User user = getAuthenticatedBusinessUser(principal);
        invoiceService.cancelInvoice(id, user);
        return new ApiMessageResponse("Invoice cancelled", LocalDateTime.now());
    }

    private User getAuthenticatedBusinessUser(Principal principal) {
        User user = userService.findByEmail(principal.getName())
                .orElseThrow(() -> new ResourceNotFoundException("Authenticated user not found"));

        if (user.getRole() != Role.BUSINESS) {
            throw new ForbiddenOperationException("This endpoint is available only for BUSINESS users");
        }
        return user;
    }
}

