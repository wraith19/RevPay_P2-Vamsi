package com.rev.app.controller;

import com.rev.app.entity.*;
import com.rev.app.service.*;
import com.opencsv.CSVWriter;
import lombok.RequiredArgsConstructor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import com.rev.app.exception.ResourceNotFoundException;

@Controller
@RequestMapping("/transactions")
@RequiredArgsConstructor
public class TransactionController {

    private static final Logger logger = LogManager.getLogger(TransactionController.class);

    private final IUserService userService;
    private final ITransactionService transactionService;

    @GetMapping("/send")
    public String sendForm(Model model) {
        return "transactions/send";
    }

    @PostMapping("/send")
    public String sendMoney(@RequestParam String recipient,
            @RequestParam BigDecimal amount,
            @RequestParam(required = false) String pin,
            @RequestParam(required = false) String note,
            Authentication authentication,
            org.springframework.web.servlet.mvc.support.RedirectAttributes redirectAttributes) {
        try {
            User sender = userService.findByEmail(authentication.getName())
                    .orElseThrow(() -> new ResourceNotFoundException("User not found"));

            String pinSetupRedirect = ensurePinConfigured(sender, redirectAttributes, "/transactions/send");
            if (pinSetupRedirect != null) {
                return pinSetupRedirect;
            }
            if (pin == null || pin.isBlank() || !userService.verifyTransactionPin(sender.getId(), pin)) {
                redirectAttributes.addFlashAttribute("error", "Invalid transaction PIN.");
                return "redirect:/transactions/send";
            }

            transactionService.sendMoney(sender, recipient, amount, note);
            redirectAttributes.addFlashAttribute("success", "Money sent successfully!");
            return "redirect:/transactions/history";
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/transactions/send";
        }
    }

    private String ensurePinConfigured(User user,
            org.springframework.web.servlet.mvc.support.RedirectAttributes redirectAttributes,
            String returnTo) {
        if (user.getTransactionPin() == null || user.getTransactionPin().isBlank()) {
            redirectAttributes.addFlashAttribute("error", "Set your transaction PIN before making transactions.");
            return "redirect:/profile/set-pin?returnTo=" + returnTo;
        }
        return null;
    }

    @GetMapping("/history")
    public String transactionHistory(@RequestParam(required = false) String type,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            @RequestParam(required = false) BigDecimal minAmount,
            @RequestParam(required = false) BigDecimal maxAmount,
            @RequestParam(required = false) String search,
            Authentication authentication,
            Model model) {
        User user = userService.findByEmail(authentication.getName())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        List<Transaction> transactions;

        if (search != null && !search.isEmpty()) {
            transactions = transactionService.searchTransactions(user, search);
        } else if (type != null || status != null || startDate != null || endDate != null
                || minAmount != null || maxAmount != null) {
            TransactionType txnType = type != null && !type.isEmpty() ? TransactionType.valueOf(type) : null;
            TransactionStatus txnStatus = status != null && !status.isEmpty() ? TransactionStatus.valueOf(status)
                    : null;
            LocalDateTime start = startDate != null && !startDate.isEmpty() ? LocalDate.parse(startDate).atStartOfDay()
                    : null;
            LocalDateTime end = endDate != null && !endDate.isEmpty() ? LocalDate.parse(endDate).atTime(LocalTime.MAX)
                    : null;
            transactions = transactionService.filterTransactions(user, txnType, txnStatus, start, end, minAmount, maxAmount);
        } else {
            transactions = transactionService.getTransactionHistory(user);
        }

        model.addAttribute("transactions", transactions);
        model.addAttribute("user", user);
        model.addAttribute("transactionTypes", TransactionType.values());
        model.addAttribute("transactionStatuses", TransactionStatus.values());

        return "transactions/history";
    }

    @GetMapping("/export/csv")
    public void exportCsv(Authentication authentication, HttpServletResponse response) throws IOException {
        User user = userService.findByEmail(authentication.getName())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        List<Transaction> transactions = transactionService.getTransactionHistory(user);

        response.setContentType("text/csv");
        response.setHeader("Content-Disposition", "attachment; filename=transactions_" +
                LocalDate.now().format(DateTimeFormatter.ISO_DATE) + ".csv");

        try (CSVWriter writer = new CSVWriter(response.getWriter())) {
            writer.writeNext(new String[] { "Transaction ID", "Date", "Type", "Amount", "Sender", "Receiver", "Status",
                    "Note" });
            for (Transaction t : transactions) {
                writer.writeNext(new String[] {
                        t.getTransactionId(),
                        t.getTimestamp().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")),
                        t.getType().name(),
                        t.getAmount().toString(),
                        t.getSender() != null ? t.getSender().getFullName() : "N/A",
                        t.getReceiver() != null ? t.getReceiver().getFullName() : "N/A",
                        t.getStatus().name(),
                        t.getNote() != null ? t.getNote() : ""
                });
            }
        }
    }

    @GetMapping("/export/pdf")
    public void exportPdf(Authentication authentication, HttpServletResponse response) throws IOException {
        User user = userService.findByEmail(authentication.getName())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        List<Transaction> transactions = transactionService.getTransactionHistory(user);

        response.setContentType("application/pdf");
        response.setHeader("Content-Disposition", "attachment; filename=transactions_" +
                LocalDate.now().format(DateTimeFormatter.ISO_DATE) + ".pdf");

        try {
            com.itextpdf.text.Document document = new com.itextpdf.text.Document();
            com.itextpdf.text.pdf.PdfWriter.getInstance(document, response.getOutputStream());
            document.open();

            com.itextpdf.text.Font titleFont = new com.itextpdf.text.Font(com.itextpdf.text.Font.FontFamily.HELVETICA,
                    18, com.itextpdf.text.Font.BOLD);
            document.add(new com.itextpdf.text.Paragraph("Transaction History - RevPay", titleFont));
            document.add(new com.itextpdf.text.Paragraph("User: " + user.getFullName()));
            document.add(new com.itextpdf.text.Paragraph("Date: " + LocalDate.now()));
            document.add(new com.itextpdf.text.Paragraph(" "));

            com.itextpdf.text.pdf.PdfPTable table = new com.itextpdf.text.pdf.PdfPTable(6);
            table.setWidthPercentage(100);
            String[] headers = { "Txn ID", "Date", "Type", "Amount", "From/To", "Status" };
            for (String h : headers) {
                com.itextpdf.text.pdf.PdfPCell cell = new com.itextpdf.text.pdf.PdfPCell(
                        new com.itextpdf.text.Phrase(h));
                cell.setBackgroundColor(com.itextpdf.text.BaseColor.LIGHT_GRAY);
                table.addCell(cell);
            }
            for (Transaction t : transactions) {
                table.addCell(t.getTransactionId());
                table.addCell(t.getTimestamp().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
                table.addCell(t.getType().name());
                table.addCell("$" + t.getAmount());
                String party = t.getSender() != null ? t.getSender().getFullName()
                        : (t.getReceiver() != null ? t.getReceiver().getFullName() : "N/A");
                table.addCell(party);
                table.addCell(t.getStatus().name());
            }
            document.add(table);
            document.close();
        } catch (com.itextpdf.text.DocumentException e) {
            throw new IOException("PDF generation failed", e);
        }
    }
}

