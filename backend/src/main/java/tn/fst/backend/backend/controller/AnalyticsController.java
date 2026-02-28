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

/**
 * Controller pour Analytics & Reporting
 */
@RestController
@RequestMapping("/api/analytics")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class AnalyticsController {

    private final AnalyticsService analyticsService;
    private final ReportExportService reportExportService;

    /**
     * Rapport quotidien
     * GET /api/analytics/sales/daily?date=2026-02-24
     */
    @GetMapping("/sales/daily")
    public ResponseEntity<DailySalesReport> getDailySalesReport(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return ResponseEntity.ok(analyticsService.getDailySalesReport(date));
    }

    /**
     * Rapport mensuel
     * GET /api/analytics/sales/monthly?year=2026&month=2
     */
    @GetMapping("/sales/monthly")
    public ResponseEntity<MonthlySalesReport> getMonthlySalesReport(
            @RequestParam int year, @RequestParam int month) {
        return ResponseEntity.ok(analyticsService.getMonthlySalesReport(year, month));
    }

    /**
     * Rapport annuel
     * GET /api/analytics/sales/yearly?year=2026
     */
    @GetMapping("/sales/yearly")
    public ResponseEntity<YearlySalesReport> getYearlySalesReport(@RequestParam int year) {
        return ResponseEntity.ok(analyticsService.getYearlySalesReport(year));
    }

    /**
     * Top produits
     * GET /api/analytics/products/top?startDate=2026-01-01&endDate=2026-12-31&limit=10
     */
    @GetMapping("/products/top")
    public ResponseEntity<TopProductsReport> getTopProducts(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(defaultValue = "10") int limit) {
        return ResponseEntity.ok(analyticsService.getTopProducts(startDate, endDate, limit));
    }

    /**
     * Analytics clients
     * GET /api/analytics/customers?startDate=2026-01-01&endDate=2026-12-31
     */
    @GetMapping("/customers")
    public ResponseEntity<CustomerAnalyticsReport> getCustomerAnalytics(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        return ResponseEntity.ok(analyticsService.getCustomerAnalytics(startDate, endDate));
    }

    /**
     * Dashboard
     * GET /api/analytics/dashboard
     */
    @GetMapping("/dashboard")
    public ResponseEntity<DashboardData> getDashboard() {
        return ResponseEntity.ok(analyticsService.getDashboardData());
    }

    /**
     * Export ventes Excel
     * GET /api/analytics/export/sales?startDate=2026-01-01&endDate=2026-12-31
     */
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

    /**
     * Export produits Excel
     * GET /api/analytics/export/products?startDate=2026-01-01&endDate=2026-12-31&limit=20
     */
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

    /**
     * Export clients Excel
     * GET /api/analytics/export/customers?startDate=2026-01-01&endDate=2026-12-31
     */
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