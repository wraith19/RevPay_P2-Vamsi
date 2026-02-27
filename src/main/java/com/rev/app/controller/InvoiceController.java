package com.rev.app.controller;

import com.rev.app.entity.*;
import com.rev.app.service.*;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import com.rev.app.exception.ResourceNotFoundException;

@Controller
@RequestMapping("/invoices")
@RequiredArgsConstructor
public class InvoiceController {

    private final IUserService userService;
    private final IInvoiceService invoiceService;

    @GetMapping
    public String listInvoices(@RequestParam(required = false) String status,
            Authentication authentication, Model model) {
        User user = userService.findByEmail(authentication.getName())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        List<Invoice> invoices;
        if (status != null && !status.isEmpty()) {
            invoices = invoiceService.getInvoicesByStatus(user, InvoiceStatus.valueOf(status));
        } else {
            invoices = invoiceService.getBusinessInvoices(user);
        }

        model.addAttribute("invoices", invoices);
        model.addAttribute("statuses", InvoiceStatus.values());
        model.addAttribute("paidTotal", invoiceService.getPaidTotal(user));
        model.addAttribute("outstandingTotal", invoiceService.getOutstandingTotal(user));
        return "invoices/list";
    }

    @GetMapping("/create")
    public String createForm(Model model) {
        model.addAttribute("invoice", new Invoice());
        return "invoices/create";
    }

    @PostMapping("/create")
    public String createInvoice(@RequestParam String customerName,
            @RequestParam(required = false) String customerEmail,
            @RequestParam(required = false) String customerAddress,
            @RequestParam(required = false) String dueDate,
            @RequestParam(required = false) String paymentTerms,
            @RequestParam(required = false) List<String> itemDescriptions,
            @RequestParam(required = false) List<Integer> itemQuantities,
            @RequestParam(required = false) List<BigDecimal> itemPrices,
            @RequestParam(required = false) List<BigDecimal> itemTaxRates,
            Authentication authentication,
            RedirectAttributes redirectAttributes) {
        try {
            User user = userService.findByEmail(authentication.getName())
                    .orElseThrow(() -> new ResourceNotFoundException("User not found"));

            LocalDate due = (dueDate != null && !dueDate.isEmpty()) ? LocalDate.parse(dueDate) : null;

            List<InvoiceItem> items = new ArrayList<>();
            if (itemDescriptions != null) {
                for (int i = 0; i < itemDescriptions.size(); i++) {
                    InvoiceItem item = InvoiceItem.builder()
                            .description(itemDescriptions.get(i))
                            .quantity(itemQuantities != null && i < itemQuantities.size() ? itemQuantities.get(i) : 1)
                            .unitPrice(
                                    itemPrices != null && i < itemPrices.size() ? itemPrices.get(i) : BigDecimal.ZERO)
                            .taxRate(itemTaxRates != null && i < itemTaxRates.size() ? itemTaxRates.get(i)
                                    : BigDecimal.ZERO)
                            .build();
                    items.add(item);
                }
            }

            invoiceService.createInvoice(user, customerName, customerEmail, customerAddress, due, paymentTerms, items);
            redirectAttributes.addFlashAttribute("success", "Invoice created successfully!");
            return "redirect:/invoices";
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/invoices/create";
        }
    }

    @GetMapping("/{id}")
    public String viewInvoice(@PathVariable Long id, Authentication authentication, Model model) {
        Invoice invoice = invoiceService.getInvoiceById(id);
        model.addAttribute("invoice", invoice);
        return "invoices/view";
    }

    @PostMapping("/{id}/send")
    public String sendInvoice(@PathVariable Long id, Authentication authentication,
            RedirectAttributes redirectAttributes) {
        try {
            User user = userService.findByEmail(authentication.getName())
                    .orElseThrow(() -> new ResourceNotFoundException("User not found"));
            invoiceService.sendInvoice(id, user);
            redirectAttributes.addFlashAttribute("success", "Invoice sent!");
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/invoices";
    }

    @PostMapping("/{id}/mark-paid")
    public String markPaid(@PathVariable Long id, Authentication authentication,
            RedirectAttributes redirectAttributes) {
        try {
            User user = userService.findByEmail(authentication.getName())
                    .orElseThrow(() -> new ResourceNotFoundException("User not found"));
            invoiceService.markAsPaid(id, user);
            redirectAttributes.addFlashAttribute("success", "Invoice marked as paid!");
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/invoices";
    }

    @PostMapping("/{id}/cancel")
    public String cancelInvoice(@PathVariable Long id, Authentication authentication,
            RedirectAttributes redirectAttributes) {
        try {
            User user = userService.findByEmail(authentication.getName())
                    .orElseThrow(() -> new ResourceNotFoundException("User not found"));
            invoiceService.cancelInvoice(id, user);
            redirectAttributes.addFlashAttribute("success", "Invoice cancelled!");
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/invoices";
    }
}

