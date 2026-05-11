package tn.fst.backend.backend.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import tn.fst.backend.backend.dto.*;
import tn.fst.backend.backend.entity.InvoiceStatus;
import tn.fst.backend.backend.entity.User;
import tn.fst.backend.backend.service.InvoiceService;

import java.io.File;
import java.time.LocalDate;
import java.util.List;


@RestController
@RequestMapping("/api/invoices")
@CrossOrigin(origins = "http://localhost:4200")
@RequiredArgsConstructor
public class InvoiceController {

    private final InvoiceService invoiceService;


    @PostMapping("/generate/{orderId}")
    public ResponseEntity<InvoiceResponse> generateInvoice(
            @PathVariable Long orderId,
            Authentication authentication) throws Exception {

        InvoiceResponse invoice = invoiceService.issueDate(orderId);
        return new ResponseEntity<>(invoice, HttpStatus.CREATED);
    }


    @GetMapping
    public ResponseEntity<List<InvoiceResponse>> getAllInvoices() {
        List<InvoiceResponse> invoices = invoiceService.getAllInvoices();
        return ResponseEntity.ok(invoices);
    }


    @GetMapping("/{invoiceId}")
    public ResponseEntity<InvoiceResponse> getInvoice(@PathVariable Long invoiceId) {
        InvoiceResponse invoice = invoiceService.getInvoice(invoiceId);
        return ResponseEntity.ok(invoice);
    }


    @GetMapping("/my-invoices")
    public ResponseEntity<List<InvoiceResponse>> getMyInvoices(Authentication authentication) {
        Long userId = getCurrentUserId(authentication);
        List<InvoiceResponse> invoices = invoiceService.getInvoicesByUser(userId);
        return ResponseEntity.ok(invoices);
    }


    @GetMapping("/status/{status}")
    public ResponseEntity<List<InvoiceResponse>> getInvoicesByStatus(
            @PathVariable InvoiceStatus status) {

        List<InvoiceResponse> invoices = invoiceService.getInvoicesByStatus(status);
        return ResponseEntity.ok(invoices);
    }

    /**
     * Obtenir les factures en retard
     * Retourne toutes les factures dont la date d'échéance est dépassée
     */
    @GetMapping("/overdue")
    public ResponseEntity<List<InvoiceResponse>> getOverdueInvoices() {
        List<InvoiceResponse> invoices = invoiceService.getOverdueInvoices();
        return ResponseEntity.ok(invoices);
    }


    @GetMapping("/{invoiceId}/download")
    public ResponseEntity<Resource> downloadInvoicePDF(@PathVariable Long invoiceId) {
        InvoiceResponse invoice;
        try {
            invoice = invoiceService.ensureInvoicePdf(invoiceId);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }

        if (invoice.getPdfPath() == null) {
            return ResponseEntity.notFound().build();
        }

        File file = new File(invoice.getPdfPath());
        if (!file.exists()) {
            return ResponseEntity.notFound().build();
        }

        Resource resource = new FileSystemResource(file);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" + invoice.getInvoiceNumber() + ".pdf\"")
                .contentType(MediaType.APPLICATION_PDF)
                .body(resource);
    }


    @PostMapping("/{invoiceId}/mark-paid")
    public ResponseEntity<InvoiceResponse> markAsPaid(
            @PathVariable Long invoiceId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate paidDate,
            Authentication authentication) {

        InvoiceResponse invoice = invoiceService.markAsPaid(invoiceId, paidDate);
        return ResponseEntity.ok(invoice);
    }


    @PostMapping("/{invoiceId}/send")
    public ResponseEntity<InvoiceResponse> sendInvoice(
            @PathVariable Long invoiceId,
            Authentication authentication) {

        InvoiceResponse invoice = invoiceService.sendInvoice(invoiceId);
        return ResponseEntity.ok(invoice);
    }


    @PostMapping("/{invoiceId}/cancel")
    public ResponseEntity<InvoiceResponse> cancelInvoice(
            @PathVariable Long invoiceId,
            @RequestParam String reason,
            Authentication authentication) {

        InvoiceResponse invoice = invoiceService.cancelInvoice(invoiceId, reason);
        return ResponseEntity.ok(invoice);
    }

    @GetMapping("/statistics")
    public ResponseEntity<FinancialStatisticsResponse> getFinancialStatistics(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

        FinancialStatisticsResponse stats = invoiceService.getFinancialStatistics(startDate, endDate);
        return ResponseEntity.ok(stats);
    }


    @GetMapping("/summary")
    public ResponseEntity<InvoiceSummaryResponse> getInvoiceSummary() {
        InvoiceSummaryResponse summary = invoiceService.getInvoiceSummary();
        return ResponseEntity.ok(summary);
    }


    @GetMapping("/tax-report")
    public ResponseEntity<TaxReportResponse> getTaxReport(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

        TaxReportResponse report = invoiceService.getTaxReport(startDate, endDate);
        return ResponseEntity.ok(report);
    }


    @GetMapping("/reconciliation")
    public ResponseEntity<PaymentReconciliationResponse> getPaymentReconciliation(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

        PaymentReconciliationResponse reconciliation =
                invoiceService.getPaymentReconciliation(startDate, endDate);
        return ResponseEntity.ok(reconciliation);
    }

    private Long getCurrentUserId(Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        return user.getIdUser();
    }
}