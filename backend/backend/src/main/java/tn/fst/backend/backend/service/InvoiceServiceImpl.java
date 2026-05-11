package tn.fst.backend.backend.service;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.*;
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
    private final AccountingAutomationService accountingAutomationService;

    private static final String INVOICE_PDF_DIR = "generated-invoices/";
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    @Override
    public InvoiceResponse issueDate(Long orderId) throws Exception {
        Order order = getOrderById(orderId);

        if (invoiceRepository.existsByOrder(order)) {
            throw new IllegalStateException("Une facture existe déjà pour cette commande");
        }

        Payment payment = paymentRepository.findByOrder(order)
                .orElseThrow(() -> new ResourceNotFoundException("Payment not found for order"));

        Invoice invoice = Invoice.builder()
                .invoiceNumber(generateInvoiceNumber())
                .order(order)
                .issueDate(LocalDate.now())
                .totalAmount(order.getTotalAmount())
                .status(payment.getStatus() == PaymentStatus.COMPLETED ? InvoiceStatus.PAID : InvoiceStatus.ISSUED)
                .build();

        invoice = invoiceRepository.save(invoice);
        accountingAutomationService.recordInvoiceIssued(invoice);

        return mapToInvoiceResponse(invoice);
    }

    @Override
    public InvoiceResponse ensureInvoicePdf(Long invoiceId) throws Exception {
        Invoice invoice = findInvoiceEntityById(invoiceId);

        if (invoice.getPdfPath() != null) {
            Path existing = Paths.get(invoice.getPdfPath());
            if (Files.exists(existing)) {
                return mapToInvoiceResponse(invoice);
            }
        }

        List<OrderDetail> orderDetails = orderDetailRepository.findByOrder(invoice.getOrder());
        String pdfPath = generateInvoicePDF(invoice, orderDetails);
        invoice.setPdfPath(pdfPath);
        invoice = invoiceRepository.save(invoice);
        return mapToInvoiceResponse(invoice);
    }

    @Override
    @Transactional(readOnly = true)
    public InvoiceResponse getInvoice(Long invoiceId) {
        Invoice invoice = findInvoiceEntityById(invoiceId);
        return mapToInvoiceResponse(invoice);
    }

    @Override
    @Transactional(readOnly = true)
    public List<InvoiceResponse> getAllInvoices() {
        List<Invoice> invoices = invoiceRepository.findAllByOrderByIssueDateDesc();
        return invoices.stream()
                .map(this::mapToInvoiceResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<InvoiceResponse> getInvoicesByUser(Long userId) {
        List<Invoice> invoices = invoiceRepository.findByOrder_User_IdUserOrderByIssueDateDesc(userId);
        return invoices.stream()
                .map(this::mapToInvoiceResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<InvoiceResponse> getInvoicesByStatus(InvoiceStatus status) {
        List<Invoice> invoices = invoiceRepository.findByStatus(status);
        return invoices.stream()
                .map(this::mapToInvoiceResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<InvoiceResponse> getOverdueInvoices() {
        List<Invoice> allInvoices = invoiceRepository.findAll();
        return allInvoices.stream()
                .filter(Invoice::isOverdue)
                .map(this::mapToInvoiceResponse)
                .collect(Collectors.toList());
    }

    @Override
    public InvoiceResponse markAsPaid(Long invoiceId, LocalDate paidDate) {
        Invoice invoice = findInvoiceEntityById(invoiceId);
        invoice.setStatus(InvoiceStatus.PAID);
        invoice = invoiceRepository.save(invoice);
        accountingAutomationService.recordInvoicePaid(invoice);
        return mapToInvoiceResponse(invoice);
    }

    @Override
    public InvoiceResponse sendInvoice(Long invoiceId) {
        Invoice invoice = findInvoiceEntityById(invoiceId);
        invoice.setStatus(InvoiceStatus.SENT);
        invoice = invoiceRepository.save(invoice);
        return mapToInvoiceResponse(invoice);
    }

    @Override
    public InvoiceResponse cancelInvoice(Long invoiceId, String reason) {
        Invoice invoice = findInvoiceEntityById(invoiceId);
        if (invoice.getStatus() == InvoiceStatus.PAID) {
            throw new IllegalStateException("Cannot cancel a paid invoice");
        }
        invoice.setStatus(InvoiceStatus.CANCELLED);
        invoice = invoiceRepository.save(invoice);
        accountingAutomationService.recordInvoiceCancelled(invoice);
        return mapToInvoiceResponse(invoice);
    }

    @Override
    @Transactional(readOnly = true)
    public FinancialStatisticsResponse getFinancialStatistics(LocalDate startDate, LocalDate endDate) {
        List<Invoice> invoices = invoiceRepository.findByIssueDateBetween(startDate, endDate);

        BigDecimal totalRevenue = invoices.stream()
                .filter(Invoice::isPaid)
                .map(Invoice::getTotalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal pendingPayments = invoices.stream()
                .filter(inv -> inv.getStatus() == InvoiceStatus.ISSUED ||
                        inv.getStatus() == InvoiceStatus.SENT)
                .map(Invoice::getTotalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return FinancialStatisticsResponse.builder()
                .totalRevenue(totalRevenue)
                .pendingPayments(pendingPayments)
                .totalInvoices((long) invoices.size())
                .paidInvoices(invoices.stream().filter(Invoice::isPaid).count())
                .overdueInvoices(invoices.stream().filter(Invoice::isOverdue).count())
                .currency("TND")
                .startDate(startDate)
                .endDate(endDate)
                .build();
    }

    @Override
    public InvoiceSummaryResponse getInvoiceSummary() {
        return InvoiceSummaryResponse.builder().build();
    }

    @Override
    public TaxReportResponse getTaxReport(LocalDate startDate, LocalDate endDate) {
        return TaxReportResponse.builder().build();
    }

    @Override
    public PaymentReconciliationResponse getPaymentReconciliation(LocalDate startDate, LocalDate endDate) {
        return PaymentReconciliationResponse.builder().build();
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
        addCellToTable(infoTable, "Commande:", headerFont);
        addCellToTable(infoTable, "ORD-" + String.format("%06d", invoice.getOrder().getIdOrder()), normalFont);
        document.add(infoTable);

        Order order = invoice.getOrder();
        PdfPTable addressTable = new PdfPTable(2);
        addressTable.setWidthPercentage(100);
        addressTable.setSpacingAfter(20);

        PdfPCell billingCell = new PdfPCell();
        billingCell.setBorder(Rectangle.BOX);
        billingCell.setPadding(10);
        billingCell.addElement(new Paragraph("CLIENT", headerFont));
        billingCell.addElement(new Paragraph(order.getUser().getUsername() + "\n" + order.getShippingAddress(), normalFont));
        addressTable.addCell(billingCell);

        PdfPCell shippingCell = new PdfPCell();
        shippingCell.setBorder(Rectangle.BOX);
        shippingCell.setPadding(10);
        shippingCell.addElement(new Paragraph("LIVRAISON", headerFont));
        shippingCell.addElement(new Paragraph(order.getShippingAddress(), normalFont));
        addressTable.addCell(shippingCell);
        document.add(addressTable);

        PdfPTable itemsTable = new PdfPTable(4);
        itemsTable.setWidthPercentage(100);
        itemsTable.setWidths(new float[] { 4, 1, 1, 1 });
        itemsTable.setSpacingAfter(20);

        addTableHeader(itemsTable, "Article", headerFont);
        addTableHeader(itemsTable, "Quantité", headerFont);
        addTableHeader(itemsTable, "Prix Unit.", headerFont);
        addTableHeader(itemsTable, "Total", headerFont);

        for (OrderDetail detail : orderDetails) {
            addCellToTable(itemsTable, detail.getVariant().getProduct().getName(), normalFont);
            addCellToTable(itemsTable, String.valueOf(detail.getQuantity()), normalFont);
            addCellToTable(itemsTable, detail.getPrice() + " TND", normalFont);
            addCellToTable(itemsTable, detail.getPrice().multiply(BigDecimal.valueOf(detail.getQuantity())) + " TND", normalFont);
        }
        document.add(itemsTable);

        PdfPTable totalsTable = new PdfPTable(2);
        totalsTable.setWidthPercentage(40);
        totalsTable.setHorizontalAlignment(Element.ALIGN_RIGHT);

        addCellToTable(totalsTable, "TOTAL:", headerFont);
        addCellToTable(totalsTable, invoice.getTotalAmount().setScale(2, RoundingMode.HALF_UP) + " TND", headerFont);
        document.add(totalsTable);

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
        Order order = invoice.getOrder();
        return InvoiceResponse.builder()
                .idInvoice(invoice.getIdInvoice())
                .invoiceNumber(invoice.getInvoiceNumber())
                .orderId(order.getIdOrder())
                .customerName(order.getUser().getUsername())
                .issueDate(invoice.getIssueDate())
                .dueDate(invoice.getIssueDate().plusDays(30))
                .totalAmount(invoice.getTotalAmount())
                .status(invoice.getStatus().name())
                .pdfPath(invoice.getPdfPath())
                .isOverdue(invoice.isOverdue())
                .build();
    }
}
