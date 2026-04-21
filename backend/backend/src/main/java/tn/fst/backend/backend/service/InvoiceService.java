package tn.fst.backend.backend.service;

import tn.fst.backend.backend.dto.*;
import tn.fst.backend.backend.entity.Invoice;
import tn.fst.backend.backend.entity.InvoiceStatus;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface InvoiceService {
    List<InvoiceResponse> getAllInvoices();

    Optional<Invoice> getInvoiceById(Long id);

    Invoice createInvoice(Invoice invoice);

    Invoice updateInvoice(Long id, Invoice invoice);

    void deleteInvoice(Long id);

    // Generate invoice for order
    InvoiceResponse issueDate(Long orderId) throws Exception;

    // Get single invoice
    InvoiceResponse getInvoice(Long invoiceId);

    // Get invoices by user
    List<InvoiceResponse> getInvoicesByUser(Long userId);

    // Get invoices by status
    List<InvoiceResponse> getInvoicesByStatus(InvoiceStatus status);

    // Get overdue invoices
    List<InvoiceResponse> getOverdueInvoices();

    // Mark as paid
    InvoiceResponse markAsPaid(Long invoiceId, LocalDate paidDate);

    // Send invoice
    InvoiceResponse sendInvoice(Long invoiceId);

    // Cancel invoice
    InvoiceResponse cancelInvoice(Long invoiceId, String reason);

    // Financial statistics
    FinancialStatisticsResponse getFinancialStatistics(LocalDate startDate, LocalDate endDate);

    // Invoice summary
    InvoiceSummaryResponse getInvoiceSummary();

    // Tax report
    TaxReportResponse getTaxReport(LocalDate startDate, LocalDate endDate);

    // Payment reconciliation
    PaymentReconciliationResponse getPaymentReconciliation(LocalDate startDate, LocalDate endDate);
}
