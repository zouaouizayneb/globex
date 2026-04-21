package tn.fst.backend.backend.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tn.fst.backend.backend.dto.*;
import tn.fst.backend.backend.service.AnalyticsService;
import tn.fst.backend.backend.service.ReportExportService;

import java.io.File;
import java.time.LocalDate;

@RestController
@RequestMapping("/api/analytics")
@CrossOrigin(origins = "http://localhost:4200")
@RequiredArgsConstructor
public class AnalyticsController {

    private final AnalyticsService analyticsService;
    private final ReportExportService reportExportService;

    @GetMapping("/sales/daily")
    public ResponseEntity<DailySalesReport> getDailySalesReport(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return ResponseEntity.ok(analyticsService.getDailySalesReport(date));
    }

    @GetMapping("/sales/monthly")
    public ResponseEntity<MonthlySalesReport> getMonthlySalesReport(
            @RequestParam int year, @RequestParam int month) {
        return ResponseEntity.ok(analyticsService.getMonthlySalesReport(year, month));
    }

    @GetMapping("/sales/yearly")
    public ResponseEntity<YearlySalesReport> getYearlySalesReport(@RequestParam int year) {
        return ResponseEntity.ok(analyticsService.getYearlySalesReport(year));
    }

    @GetMapping("/products/top")
    public ResponseEntity<TopProductsReport> getTopProducts(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(defaultValue = "10") int limit) {
        return ResponseEntity.ok(analyticsService.getTopProducts(startDate, endDate, limit));
    }

    @GetMapping("/customers")
    public ResponseEntity<CustomerAnalyticsReport> getCustomerAnalytics(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        return ResponseEntity.ok(analyticsService.getCustomerAnalytics(startDate, endDate));
    }

    @GetMapping("/dashboard")
    public ResponseEntity<DashboardData> getDashboard() {
        return ResponseEntity.ok(analyticsService.getDashboardData());
    }

    @GetMapping("/export/sales")
    public ResponseEntity<Resource> exportSalesReport(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) throws Exception {

        String filePath = reportExportService.exportSalesReportToExcel(startDate, endDate);
        File file = new File(filePath);
        if (!file.exists()) return ResponseEntity.notFound().build();

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + file.getName() + "\"")
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(new FileSystemResource(file));
    }

    @GetMapping("/export/products")
    public ResponseEntity<Resource> exportTopProducts(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(defaultValue = "20") int limit) throws Exception {

        String filePath = reportExportService.exportTopProductsToExcel(startDate, endDate, limit);
        File file = new File(filePath);
        if (!file.exists()) return ResponseEntity.notFound().build();

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + file.getName() + "\"")
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(new FileSystemResource(file));
    }

    @GetMapping("/export/customers")
    public ResponseEntity<Resource> exportCustomerAnalytics(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) throws Exception {

        String filePath = reportExportService.exportCustomerAnalyticsToExcel(startDate, endDate);
        File file = new File(filePath);
        if (!file.exists()) return ResponseEntity.notFound().build();

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + file.getName() + "\"")
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(new FileSystemResource(file));
    }
}