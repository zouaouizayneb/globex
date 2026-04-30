package tn.fst.backend.backend.service;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.*;
import lombok.Builder;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tn.fst.backend.backend.dto.*;
import tn.fst.backend.backend.entity.*;
import tn.fst.backend.backend.exeptions.ResourceNotFoundException;
import tn.fst.backend.backend.repository.*;

import java.io.FileOutputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class InvoiceServiceImpl implements InvoiceService {

    private final InvoiceRepository invoiceRepository;
    private final OrderRepository orderRepository;
    private final OrderDetailRepository orderDetailRepository;
    private final PaymentRepository paymentRepository;
    private final TaxService taxService;
    private final AccountingAutomationService accountingAutomationService;

    private static final String INVOICE_PDF_DIR = "generated-invoices/";
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    /**
     * Générer automatiquement une facture pour une commande
     */
    @Override
    public InvoiceResponse issueDate(Long orderId) throws Exception {
        Order order = getOrderById(orderId);

        if (invoiceRepository.existsByOrder(order)) {
            throw new IllegalStateException("Une facture existe déjà pour cette commande");
        }

        Payment payment = paymentRepository.findByOrder(order)
                .orElseThrow(() -> new ResourceNotFoundException("Payment not found for order"));

        List<OrderDetail> orderDetails = orderDetailRepository.findByOrder(order);

        BigDecimal subtotal = orderDetails.stream()
                .map(detail -> detail.getPrice().multiply(BigDecimal.valueOf(detail.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        Shipment shipment = order.getShipment();
        Address shippingAddress = shipment != null ? shipment.getShippingAddress() : null;

        TaxCalculationResponse taxCalc = taxService.calculateTax(
                TaxCalculationRequest.builder()
                        .subtotal(subtotal)
                        .country(shippingAddress != null ? shippingAddress.getCountry() : "TN")
                        .state(shippingAddress != null ? shippingAddress.getState() : null)
                        .build());

        BigDecimal shippingCost = shipment != null ? shipment.getShippingCost() : BigDecimal.ZERO;

        Invoice invoice = Invoice.builder()
                .invoiceNumber(generateInvoiceNumber())
                .order(order)
                .user(order.getUser())
                .issueDate(LocalDate.now())
                .dueDate(LocalDate.now().plusDays(30)) // 30 jours pour payer
                .paidDate(payment.getStatus() == PaymentStatus.COMPLETED ? LocalDate.now() : null)
                .subtotal(subtotal)
                .taxAmount(taxCalc.getTaxAmount())
                .taxRate(taxCalc.getTaxRate())
                .taxType(taxCalc.getTaxType())
                .shippingCost(shippingCost)
                .discountAmount(BigDecimal.ZERO)
                .totalAmount(subtotal.add(taxCalc.getTaxAmount()).add(shippingCost))
                .currency("TND")
                .billingAddress(shippingAddress != null ? shippingAddress.getFormattedAddress() : "")
                .shippingAddress(shippingAddress != null ? shippingAddress.getFormattedAddress() : "")
                .status(payment.getStatus() == PaymentStatus.COMPLETED ? InvoiceStatus.PAID : InvoiceStatus.ISSUED)
                .paymentMethod(payment.getPaymentMethod().name())
                .paymentReference(payment.getTransactionId())
                .build();

        invoice = invoiceRepository.save(invoice);
        accountingAutomationService.recordInvoiceIssued(invoice);

        // Keep sale flow fast: generate PDF lazily on preview/download.
        invoice.setPdfGenerated(false);
        invoice.setPdfPath(null);
        invoice = invoiceRepository.save(invoice);

        return mapToInvoiceResponse(invoice);
    }

    @Override
    public InvoiceResponse ensureInvoicePdf(Long invoiceId) throws Exception {
        Invoice invoice = findInvoiceEntityById(invoiceId);

        if (Boolean.TRUE.equals(invoice.getPdfGenerated()) && invoice.getPdfPath() != null) {
            Path existing = Paths.get(invoice.getPdfPath());
            if (Files.exists(existing)) {
                return mapToInvoiceResponse(invoice);
            }
        }

        List<OrderDetail> orderDetails = orderDetailRepository.findByOrder(invoice.getOrder());
        String pdfPath = generateInvoicePDF(invoice, orderDetails);
        invoice.setPdfPath(pdfPath);
        invoice.setPdfGenerated(true);
        invoice = invoiceRepository.save(invoice);
        return mapToInvoiceResponse(invoice);
    }

    /**
     * Obtenir une facture par ID
     */
    @Override
    @Transactional(readOnly = true)
    public InvoiceResponse getInvoice(Long invoiceId) {
        Invoice invoice = findInvoiceEntityById(invoiceId);
        return mapToInvoiceResponse(invoice);
    }

    /**
     * Obtenir toutes les factures
     */
    @Override
    @Transactional(readOnly = true)
    public List<InvoiceResponse> getAllInvoices() {
        List<Invoice> invoices = invoiceRepository.findAllByOrderByIssueDateDesc();
        return invoices.stream()
                .map(this::mapToInvoiceResponse)
                .collect(Collectors.toList());
    }

    /**
     * Obtenir les factures d'un client
     */
    @Override
    @Transactional(readOnly = true)
    public List<InvoiceResponse> getInvoicesByUser(Long userId) {
        List<Invoice> invoices = invoiceRepository.findByUser_IdUserOrderByIssueDateDesc(userId);
        return invoices.stream()
                .map(this::mapToInvoiceResponse)
                .collect(Collectors.toList());
    }

    /**
     * Obtenir les factures par statut
     */
    @Override
    @Transactional(readOnly = true)
    public List<InvoiceResponse> getInvoicesByStatus(InvoiceStatus status) {
        List<Invoice> invoices = invoiceRepository.findByStatus(status);
        return invoices.stream()
                .map(this::mapToInvoiceResponse)
                .collect(Collectors.toList());
    }

    /**
     * Obtenir les factures en retard
     */
    @Override
    @Transactional(readOnly = true)
    public List<InvoiceResponse> getOverdueInvoices() {
        List<Invoice> allInvoices = invoiceRepository.findAll();
        return allInvoices.stream()
                .filter(Invoice::isOverdue)
                .map(this::mapToInvoiceResponse)
                .collect(Collectors.toList());
    }

    /**
     * Marquer une facture comme payée
     */
    @Override
    public InvoiceResponse markAsPaid(Long invoiceId, LocalDate paidDate) {
        Invoice invoice = findInvoiceEntityById(invoiceId);

        invoice.setStatus(InvoiceStatus.PAID);
        invoice.setPaidDate(paidDate != null ? paidDate : LocalDate.now());

        invoice = invoiceRepository.save(invoice);
        accountingAutomationService.recordInvoicePaid(invoice);
        return mapToInvoiceResponse(invoice);
    }

    /**
     * Envoyer une facture par email (TODO)
     */
    @Override
    public InvoiceResponse sendInvoice(Long invoiceId) {
        Invoice invoice = findInvoiceEntityById(invoiceId);

        // TODO: Implémenter l'envoi d'email avec le PDF en pièce jointe

        invoice.setStatus(InvoiceStatus.SENT);
        invoice = invoiceRepository.save(invoice);

        return mapToInvoiceResponse(invoice);
    }

    /**
     * Annuler une facture
     */
    @Override
    public InvoiceResponse cancelInvoice(Long invoiceId, String reason) {
        Invoice invoice = findInvoiceEntityById(invoiceId);

        if (invoice.getStatus() == InvoiceStatus.PAID) {
            throw new IllegalStateException("Cannot cancel a paid invoice");
        }

        invoice.setStatus(InvoiceStatus.CANCELLED);
        invoice.setInternalNotes(
                (invoice.getInternalNotes() != null ? invoice.getInternalNotes() + "\n" : "") +
                        "CANCELLED: " + reason);

        invoice = invoiceRepository.save(invoice);
        accountingAutomationService.recordInvoiceCancelled(invoice);
        return mapToInvoiceResponse(invoice);
    }

    /**
     * Obtenir les statistiques financières
     */
    @Override
    @Transactional(readOnly = true)
    public FinancialStatisticsResponse getFinancialStatistics(LocalDate startDate, LocalDate endDate) {
        List<Invoice> invoices = invoiceRepository.findByIssueDateBetween(startDate, endDate);

        BigDecimal totalRevenue = invoices.stream()
                .filter(Invoice::isPaid)
                .map(Invoice::getTotalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalTax = invoices.stream()
                .filter(Invoice::isPaid)
                .map(Invoice::getTaxAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal pendingPayments = invoices.stream()
                .filter(inv -> inv.getStatus() == InvoiceStatus.ISSUED ||
                        inv.getStatus() == InvoiceStatus.SENT)
                .map(Invoice::getTotalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        long totalInvoices = invoices.size();
        long paidInvoices = invoices.stream().filter(Invoice::isPaid).count();
        long overdueInvoices = invoices.stream().filter(Invoice::isOverdue).count();

        return FinancialStatisticsResponse.builder()
                .totalRevenue(totalRevenue)
                .totalTax(totalTax)
                .pendingPayments(pendingPayments)
                .totalInvoices(totalInvoices)
                .paidInvoices(paidInvoices)
                .overdueInvoices(overdueInvoices)
                .currency("TND")
                .startDate(startDate)
                .endDate(endDate)
                .build();
    }

    @Override
    public InvoiceSummaryResponse getInvoiceSummary() {
        return InvoiceSummaryResponse.builder().build(); // TODO: Implement fully
    }

    @Override
    public TaxReportResponse getTaxReport(LocalDate startDate, LocalDate endDate) {
        return TaxReportResponse.builder().build(); // TODO: Implement fully
    }

    @Override
    public PaymentReconciliationResponse getPaymentReconciliation(LocalDate startDate, LocalDate endDate) {
        return PaymentReconciliationResponse.builder().build(); // TODO: Implement fully
    }

    @Override
    @Transactional(readOnly = true)
    public java.util.Optional<Invoice> getInvoiceById(Long id) {
        return invoiceRepository.findById(id);
    }

    @Override
    public Invoice createInvoice(Invoice invoice) {
        return invoiceRepository.save(invoice);
    }

    @Override
    public Invoice updateInvoice(Long id, Invoice invoice) {
        invoice.setIdInvoice(id);
        return invoiceRepository.save(invoice);
    }

    @Override
    public void deleteInvoice(Long id) {
        invoiceRepository.deleteById(id);
    }

    /**
     * Générer le PDF de la facture
     */
    private String generateInvoicePDF(Invoice invoice, List<OrderDetail> orderDetails) throws Exception {
        Path dir = Paths.get(INVOICE_PDF_DIR);
        if (!Files.exists(dir)) {
            Files.createDirectories(dir);
        }

        String fileName = invoice.getInvoiceNumber() + ".pdf";
        String filePath = INVOICE_PDF_DIR + fileName;

        Document document = new Document(PageSize.A4);
        PdfWriter.getInstance(document, new FileOutputStream(filePath));

        document.open();

        Font titleFont = new Font(Font.FontFamily.HELVETICA, 20, Font.BOLD);
        Font headerFont = new Font(Font.FontFamily.HELVETICA, 12, Font.BOLD);
        Font normalFont = new Font(Font.FontFamily.HELVETICA, 10);
        Font smallFont = new Font(Font.FontFamily.HELVETICA, 8);

        Paragraph company = new Paragraph("GLOBEX E-COMMERCE", titleFont);
        company.setAlignment(Element.ALIGN_CENTER);
        document.add(company);

        Paragraph companyAddress = new Paragraph(
                "123 Avenue Habib Bourguiba, Tunis, Tunisia\nTel: +216 71 123 456\nEmail: contact@globex.tn",
                smallFont);
        companyAddress.setAlignment(Element.ALIGN_CENTER);
        companyAddress.setSpacingAfter(20);
        document.add(companyAddress);

        Paragraph title = new Paragraph("FACTURE", headerFont);
        title.setAlignment(Element.ALIGN_CENTER);
        title.setSpacingAfter(20);
        document.add(title);

        PdfPTable infoTable = new PdfPTable(2);
        infoTable.setWidthPercentage(100);
        infoTable.setSpacingAfter(20);

        addCellToTable(infoTable, "Numéro de facture:", headerFont);
        addCellToTable(infoTable, invoice.getInvoiceNumber(), normalFont);

        addCellToTable(infoTable, "Date d'émission:", headerFont);
        addCellToTable(infoTable, invoice.getIssueDate().format(DATE_FORMATTER), normalFont);

        addCellToTable(infoTable, "Date d'échéance:", headerFont);
        addCellToTable(infoTable, invoice.getDueDate() != null ? invoice.getDueDate().format(DATE_FORMATTER) : "-",
                normalFont);

        addCellToTable(infoTable, "Commande:", headerFont);
        addCellToTable(infoTable, "ORD-" + String.format("%06d", invoice.getOrder().getIdOrder()), normalFont);

        document.add(infoTable);

        PdfPTable addressTable = new PdfPTable(2);
        addressTable.setWidthPercentage(100);
        addressTable.setSpacingAfter(20);

        PdfPCell billingCell = new PdfPCell();
        billingCell.setBorder(Rectangle.BOX);
        billingCell.setPadding(10);
        Paragraph billingTitle = new Paragraph("FACTURATION", headerFont);
        billingTitle.setSpacingAfter(5);
        billingCell.addElement(billingTitle);
        billingCell.addElement(
                new Paragraph(invoice.getUser().getUsername() + "\n" + invoice.getBillingAddress(), normalFont));
        addressTable.addCell(billingCell);

        PdfPCell shippingCell = new PdfPCell();
        shippingCell.setBorder(Rectangle.BOX);
        shippingCell.setPadding(10);
        Paragraph shippingTitle = new Paragraph("LIVRAISON", headerFont);
        shippingTitle.setSpacingAfter(5);
        shippingCell.addElement(shippingTitle);
        shippingCell.addElement(new Paragraph(invoice.getShippingAddress(), normalFont));
        addressTable.addCell(shippingCell);

        document.add(addressTable);

        PdfPTable itemsTable = new PdfPTable(5);
        itemsTable.setWidthPercentage(100);
        itemsTable.setWidths(new float[] { 3, 1, 1, 1, 1 });
        itemsTable.setSpacingAfter(20);

        addTableHeader(itemsTable, "Article", headerFont);
        addTableHeader(itemsTable, "Quantité", headerFont);
        addTableHeader(itemsTable, "Prix Unit.", headerFont);
        addTableHeader(itemsTable, "TVA", headerFont);
        addTableHeader(itemsTable, "Total", headerFont);

        // Items
        for (OrderDetail detail : orderDetails) {
            addCellToTable(itemsTable, detail.getVariant().getProduct().getName() +
                    " (" + detail.getVariant().getColor() + " - " +
                    detail.getVariant().getSize() + ")", normalFont);
            addCellToTable(itemsTable, String.valueOf(detail.getQuantity()), normalFont);
            addCellToTable(itemsTable, detail.getPrice() + " TND", normalFont);
            addCellToTable(itemsTable, invoice.getTaxRate() + "%", normalFont);
            addCellToTable(itemsTable, detail.getPrice().multiply(BigDecimal.valueOf(detail.getQuantity())) + " TND",
                    normalFont);
        }

        document.add(itemsTable);

        PdfPTable totalsTable = new PdfPTable(2);
        totalsTable.setWidthPercentage(40);
        totalsTable.setHorizontalAlignment(Element.ALIGN_RIGHT);

        addCellToTable(totalsTable, "Sous-total:", normalFont);
        addCellToTable(totalsTable,
                invoice.getSubtotal().setScale(2, RoundingMode.HALF_UP) + " " + invoice.getCurrency(), normalFont);

        addCellToTable(totalsTable, "Livraison:", normalFont);
        addCellToTable(totalsTable,
                invoice.getShippingCost().setScale(2, RoundingMode.HALF_UP) + " " + invoice.getCurrency(), normalFont);

        addCellToTable(totalsTable, "TVA (" + invoice.getTaxRate() + "%):", normalFont);
        addCellToTable(totalsTable,
                invoice.getTaxAmount().setScale(2, RoundingMode.HALF_UP) + " " + invoice.getCurrency(), normalFont);

        PdfPCell totalLabelCell = new PdfPCell(new Phrase("TOTAL:", headerFont));
        totalLabelCell.setBorder(Rectangle.TOP);
        totalLabelCell.setPadding(5);
        totalsTable.addCell(totalLabelCell);

        PdfPCell totalAmountCell = new PdfPCell(new Phrase(
                invoice.getTotalAmount().setScale(2, RoundingMode.HALF_UP) + " " + invoice.getCurrency(), headerFont));
        totalAmountCell.setBorder(Rectangle.TOP);
        totalAmountCell.setPadding(5);
        totalsTable.addCell(totalAmountCell);

        document.add(totalsTable);

        Paragraph footer = new Paragraph("\n\nMerci pour votre achat!", smallFont);
        footer.setAlignment(Element.ALIGN_CENTER);
        document.add(footer);

        if (invoice.isPaid()) {
            Paragraph paid = new Paragraph("PAYÉE - " + invoice.getPaidDate().format(DATE_FORMATTER), headerFont);
            paid.setAlignment(Element.ALIGN_CENTER);
            document.add(paid);
        }

        document.close();

        return filePath;
    }

    private void addTableHeader(PdfPTable table, String text, Font font) {
        PdfPCell cell = new PdfPCell(new Phrase(text, font));
        cell.setBackgroundColor(BaseColor.LIGHT_GRAY);
        cell.setPadding(5);
        table.addCell(cell);
    }

    private void addCellToTable(PdfPTable table, String text, Font font) {
        PdfPCell cell = new PdfPCell(new Phrase(text, font));
        cell.setPadding(5);
        table.addCell(cell);
    }

    private String generateInvoiceNumber() {
        long count = invoiceRepository.count();
        int year = LocalDate.now().getYear();
        return String.format("INV-%d-%06d", year, count + 1);
    }

    private Invoice findInvoiceEntityById(Long id) {
        return invoiceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Invoice", id));
    }

    private Order getOrderById(Long id) {
        return orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Order", id));
    }

    private InvoiceResponse mapToInvoiceResponse(Invoice invoice) {
        return InvoiceResponse.builder()
                .invoiceId(invoice.getIdInvoice())
                .invoiceNumber(invoice.getInvoiceNumber())
                .orderId(invoice.getOrder().getIdOrder())
                .orderNumber("ORD-" + String.format("%06d", invoice.getOrder().getIdOrder()))
                .userId(invoice.getUser().getIdUser())
                .customerName(invoice.getUser().getUsername())
                .issueDate(invoice.getIssueDate())
                .dueDate(invoice.getDueDate())
                .paidDate(invoice.getPaidDate())
                .subtotal(invoice.getSubtotal())
                .taxAmount(invoice.getTaxAmount())
                .taxRate(invoice.getTaxRate())
                .taxType(invoice.getTaxType())
                .shippingCost(invoice.getShippingCost())
                .discountAmount(invoice.getDiscountAmount())
                .totalAmount(invoice.getTotalAmount())
                .currency(invoice.getCurrency())
                .billingAddress(invoice.getBillingAddress())
                .shippingAddress(invoice.getShippingAddress())
                .status(invoice.getStatus().name())
                .paymentMethod(invoice.getPaymentMethod())
                .paymentReference(invoice.getPaymentReference())
                .pdfPath(invoice.getPdfPath())
                .pdfGenerated(invoice.getPdfGenerated())
                .isOverdue(invoice.isOverdue())
                .daysOverdue(invoice.getDaysOverdue())
                .notes(invoice.getNotes())
                .createdAt(invoice.getCreatedAt())
                .build();
    }
}
