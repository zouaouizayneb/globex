package tn.fst.backend.backend.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tn.fst.backend.backend.entity.AccountingEntry;
import tn.fst.backend.backend.entity.AccountingEntryType;
import tn.fst.backend.backend.entity.Invoice;
import tn.fst.backend.backend.repository.AccountingEntryRepository;

@Service
@RequiredArgsConstructor
@Transactional
public class AccountingAutomationService {

    private final AccountingEntryRepository accountingEntryRepository;

    public void recordInvoiceIssued(Invoice invoice) {
        createEntry(
                AccountingEntryType.INVOICE_ISSUED,
                invoice,
                "Invoice issued for order #" + invoice.getOrder().getIdOrder()
        );
    }

    public void recordInvoicePaid(Invoice invoice) {
        createEntry(
                AccountingEntryType.INVOICE_PAID,
                invoice,
                "Invoice paid: " + invoice.getInvoiceNumber()
        );
    }

    public void recordInvoiceCancelled(Invoice invoice) {
        createEntry(
                AccountingEntryType.INVOICE_CANCELLED,
                invoice,
                "Invoice cancelled: " + invoice.getInvoiceNumber()
        );
    }

    public void recordInvoiceRefunded(Invoice invoice) {
        createEntry(
                AccountingEntryType.INVOICE_REFUNDED,
                invoice,
                "Invoice refunded: " + invoice.getInvoiceNumber()
        );
    }

    private void createEntry(AccountingEntryType type, Invoice invoice, String description) {
        accountingEntryRepository.save(
                AccountingEntry.builder()
                        .type(type)
                        .referenceType("INVOICE")
                        .referenceId(invoice.getIdInvoice())
                        .amount(invoice.getTotalAmount())
                        .currency("TND")
                        .description(description)
                        .build()
        );
    }
}
