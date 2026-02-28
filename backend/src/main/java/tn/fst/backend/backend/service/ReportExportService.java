package tn.fst.backend.backend.service;

import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import tn.fst.backend.backend.dto.*;

import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Service d'export de rapports en Excel
 * Utilise Apache POI pour générer les fichiers Excel
 */
@Service
@RequiredArgsConstructor
public class ReportExportService {

    private final AnalyticsService analyticsService;

    private static final String EXPORT_DIR = "/mnt/user-data/outputs/reports/";
    private static final DateTimeFormatter FILE_DATE_FORMAT = DateTimeFormatter.ofPattern("yyyyMMdd");
    private static final DateTimeFormatter DISPLAY_DATE_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    /**
     * Exporter rapport de ventes en Excel
     */
    public String exportSalesReportToExcel(LocalDate startDate, LocalDate endDate) throws IOException {
        ensureDirectoryExists();

        String fileName = String.format("Sales_Report_%s_to_%s.xlsx",
                startDate.format(FILE_DATE_FORMAT),
                endDate.format(FILE_DATE_FORMAT));
        String filePath = EXPORT_DIR + fileName;

        // Récupérer les données
        DailySalesReport report = analyticsService.getDailySalesReport(startDate);

        // Créer le workbook
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Sales Report");

            int rowNum = 0;

            // Title
            Row titleRow = sheet.createRow(rowNum++);
            Cell titleCell = titleRow.createCell(0);
            titleCell.setCellValue("SALES REPORT");
            titleCell.setCellStyle(createTitleStyle(workbook));
            sheet.addMergedRegion(new org.apache.poi.ss.util.CellRangeAddress(0, 0, 0, 5));

            // Period
            Row periodRow = sheet.createRow(rowNum++);
            periodRow.createCell(0).setCellValue("Period: " + startDate.format(DISPLAY_DATE_FORMAT) +
                    " to " + endDate.format(DISPLAY_DATE_FORMAT));
            sheet.addMergedRegion(new org.apache.poi.ss.util.CellRangeAddress(1, 1, 0, 5));

            rowNum++; // Empty row

            // Summary
            CellStyle boldStyle = createBoldStyle(workbook);
            Row summaryHeaderRow = sheet.createRow(rowNum++);
            summaryHeaderRow.createCell(0).setCellValue("SUMMARY");
            summaryHeaderRow.getCell(0).setCellStyle(boldStyle);

            Row totalOrdersRow = sheet.createRow(rowNum++);
            totalOrdersRow.createCell(0).setCellValue("Total Orders:");
            totalOrdersRow.createCell(1).setCellValue(report.getTotalOrders());

            Row totalRevenueRow = sheet.createRow(rowNum++);
            totalRevenueRow.createCell(0).setCellValue("Total Revenue:");
            totalRevenueRow.createCell(1).setCellValue(report.getTotalRevenue().doubleValue() + " TND");

            Row avgOrderRow = sheet.createRow(rowNum++);
            avgOrderRow.createCell(0).setCellValue("Average Order Value:");
            avgOrderRow.createCell(1).setCellValue(report.getAverageOrderValue().doubleValue() + " TND");

            rowNum++; // Empty row

            // Top Products
            Row topProductsHeaderRow = sheet.createRow(rowNum++);
            topProductsHeaderRow.createCell(0).setCellValue("TOP PRODUCTS");
            topProductsHeaderRow.getCell(0).setCellStyle(boldStyle);

            // Headers
            Row headerRow = sheet.createRow(rowNum++);
            String[] headers = {"Rank", "Product Name", "SKU", "Qty Sold", "Revenue (TND)"};
            CellStyle headerStyle = createHeaderStyle(workbook);
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }

            // Data
            List<TopProductItem> topProducts = report.getTopProducts();
            for (int i = 0; i < topProducts.size(); i++) {
                TopProductItem product = topProducts.get(i);
                Row dataRow = sheet.createRow(rowNum++);
                dataRow.createCell(0).setCellValue(i + 1);
                dataRow.createCell(1).setCellValue(product.getProductName());
                dataRow.createCell(2).setCellValue(product.getSku());
                dataRow.createCell(3).setCellValue(product.getQuantitySold());
                dataRow.createCell(4).setCellValue(product.getRevenue().doubleValue());
            }

            rowNum++; // Empty row

            // Payment Methods
            Row paymentHeaderRow = sheet.createRow(rowNum++);
            paymentHeaderRow.createCell(0).setCellValue("SALES BY PAYMENT METHOD");
            paymentHeaderRow.getCell(0).setCellStyle(boldStyle);

            Row paymentHeaderRow2 = sheet.createRow(rowNum++);
            String[] paymentHeaders = {"Payment Method", "Orders", "Amount (TND)"};
            for (int i = 0; i < paymentHeaders.length; i++) {
                Cell cell = paymentHeaderRow2.createCell(i);
                cell.setCellValue(paymentHeaders[i]);
                cell.setCellStyle(headerStyle);
            }

            for (PaymentMethodSales pms : report.getSalesByPaymentMethod()) {
                Row dataRow = sheet.createRow(rowNum++);
                dataRow.createCell(0).setCellValue(pms.getPaymentMethod());
                dataRow.createCell(1).setCellValue(pms.getOrderCount());
                dataRow.createCell(2).setCellValue(pms.getTotalAmount().doubleValue());
            }

            // Auto-size columns
            for (int i = 0; i < 6; i++) {
                sheet.autoSizeColumn(i);
            }

            // Write to file
            try (FileOutputStream outputStream = new FileOutputStream(filePath)) {
                workbook.write(outputStream);
            }
        }

        return filePath;
    }

    /**
     * Exporter top produits en Excel
     */
    public String exportTopProductsToExcel(LocalDate startDate, LocalDate endDate, int limit) throws IOException {
        ensureDirectoryExists();

        String fileName = String.format("Top_Products_%s_to_%s.xlsx",
                startDate.format(FILE_DATE_FORMAT),
                endDate.format(FILE_DATE_FORMAT));
        String filePath = EXPORT_DIR + fileName;

        TopProductsReport report = analyticsService.getTopProducts(startDate, endDate, limit);

        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Top Products");

            int rowNum = 0;

            // Title
            Row titleRow = sheet.createRow(rowNum++);
            Cell titleCell = titleRow.createCell(0);
            titleCell.setCellValue("TOP PRODUCTS REPORT");
            titleCell.setCellStyle(createTitleStyle(workbook));
            sheet.addMergedRegion(new org.apache.poi.ss.util.CellRangeAddress(0, 0, 0, 5));

            rowNum++; // Empty row

            // Headers
            Row headerRow = sheet.createRow(rowNum++);
            String[] headers = {"Rank", "Product Name", "Category", "Units Sold", "Revenue (TND)", "Avg Price"};
            CellStyle headerStyle = createHeaderStyle(workbook);
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }

            // Data
            List<TopProductDetail> products = report.getTopProducts();
            for (int i = 0; i < products.size(); i++) {
                TopProductDetail product = products.get(i);
                Row dataRow = sheet.createRow(rowNum++);
                dataRow.createCell(0).setCellValue(i + 1);
                dataRow.createCell(1).setCellValue(product.getProductName());
                dataRow.createCell(2).setCellValue(product.getCategory());
                dataRow.createCell(3).setCellValue(product.getQuantitySold());
                dataRow.createCell(4).setCellValue(product.getRevenue().doubleValue());
                dataRow.createCell(5).setCellValue(product.getAveragePrice().doubleValue());
            }

            // Auto-size columns
            for (int i = 0; i < 6; i++) {
                sheet.autoSizeColumn(i);
            }

            try (FileOutputStream outputStream = new FileOutputStream(filePath)) {
                workbook.write(outputStream);
            }
        }

        return filePath;
    }

    /**
     * Exporter analytics clients en Excel
     */
    public String exportCustomerAnalyticsToExcel(LocalDate startDate, LocalDate endDate) throws IOException {
        ensureDirectoryExists();

        String fileName = String.format("Customer_Analytics_%s_to_%s.xlsx",
                startDate.format(FILE_DATE_FORMAT),
                endDate.format(FILE_DATE_FORMAT));
        String filePath = EXPORT_DIR + fileName;

        CustomerAnalyticsReport report = analyticsService.getCustomerAnalytics(startDate, endDate);

        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Customer Analytics");

            int rowNum = 0;

            // Title
            Row titleRow = sheet.createRow(rowNum++);
            Cell titleCell = titleRow.createCell(0);
            titleCell.setCellValue("CUSTOMER ANALYTICS REPORT");
            titleCell.setCellStyle(createTitleStyle(workbook));
            sheet.addMergedRegion(new org.apache.poi.ss.util.CellRangeAddress(0, 0, 0, 4));

            rowNum++; // Empty row

            // Summary
            CellStyle boldStyle = createBoldStyle(workbook);
            Row summaryRow = sheet.createRow(rowNum++);
            summaryRow.createCell(0).setCellValue("Total Customers:");
            summaryRow.createCell(1).setCellValue(report.getTotalCustomers());

            Row activeRow = sheet.createRow(rowNum++);
            activeRow.createCell(0).setCellValue("Active Customers:");
            activeRow.createCell(1).setCellValue(report.getActiveCustomers());

            Row newRow = sheet.createRow(rowNum++);
            newRow.createCell(0).setCellValue("New Customers:");
            newRow.createCell(1).setCellValue(report.getNewCustomers());

            Row avgRow = sheet.createRow(rowNum++);
            avgRow.createCell(0).setCellValue("Average Customer Value:");
            avgRow.createCell(1).setCellValue(report.getAverageCustomerValue().doubleValue() + " TND");

            rowNum++; // Empty row

            // Top Customers
            Row topHeaderRow = sheet.createRow(rowNum++);
            topHeaderRow.createCell(0).setCellValue("TOP CUSTOMERS");
            topHeaderRow.getCell(0).setCellStyle(boldStyle);

            Row headerRow = sheet.createRow(rowNum++);
            String[] headers = {"Rank", "Customer Name", "Email", "Orders", "Total Spent (TND)"};
            CellStyle headerStyle = createHeaderStyle(workbook);
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }

            List<TopCustomer> customers = report.getTopCustomers();
            for (int i = 0; i < customers.size(); i++) {
                TopCustomer customer = customers.get(i);
                Row dataRow = sheet.createRow(rowNum++);
                dataRow.createCell(0).setCellValue(i + 1);
                dataRow.createCell(1).setCellValue(customer.getCustomerName());
                dataRow.createCell(2).setCellValue(customer.getEmail());
                dataRow.createCell(3).setCellValue(customer.getTotalOrders());
                dataRow.createCell(4).setCellValue(customer.getTotalSpent().doubleValue());
            }

            for (int i = 0; i < 5; i++) {
                sheet.autoSizeColumn(i);
            }

            try (FileOutputStream outputStream = new FileOutputStream(filePath)) {
                workbook.write(outputStream);
            }
        }

        return filePath;
    }

    // ==================== HELPER METHODS ====================

    private void ensureDirectoryExists() throws IOException {
        Path dir = Paths.get(EXPORT_DIR);
        if (!Files.exists(dir)) {
            Files.createDirectories(dir);
        }
    }

    private CellStyle createTitleStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        font.setFontHeightInPoints((short) 16);
        style.setFont(font);
        style.setAlignment(HorizontalAlignment.CENTER);
        return style;
    }

    private CellStyle createHeaderStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        font.setColor(IndexedColors.WHITE.getIndex());
        style.setFont(font);
        style.setFillForegroundColor(IndexedColors.DARK_BLUE.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        return style;
    }

    private CellStyle createBoldStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        style.setFont(font);
        return style;
    }

    public long getFileSize(String filePath) {
        return Paths.get(filePath).toFile().length();
    }
}