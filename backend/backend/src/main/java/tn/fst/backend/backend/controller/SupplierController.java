package tn.fst.backend.backend.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import tn.fst.backend.backend.dto.*;
import tn.fst.backend.backend.entity.SupplierStatus;
import tn.fst.backend.backend.service.SupplierService;

import java.time.LocalDate;
import java.util.List;


@RestController
@RequestMapping("/api/suppliers")
@CrossOrigin(origins = "http://localhost:4200")
@RequiredArgsConstructor
public class SupplierController {

    private final SupplierService supplierService;

    @PostMapping
    public ResponseEntity<SupplierResponse> createSupplier(
            @Valid @RequestBody SupplierRequest request,
            Authentication authentication) {

        SupplierResponse response = supplierService.createSupplier(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<SupplierResponse>> getAllActiveSuppliers() {
        List<SupplierResponse> suppliers = supplierService.getAllActiveSuppliers();
        return ResponseEntity.ok(suppliers);
    }

    @GetMapping("/all")
    public ResponseEntity<List<SupplierResponse>> getAllSuppliers() {
        List<SupplierResponse> suppliers = supplierService.getAllSuppliers();
        return ResponseEntity.ok(suppliers);
    }

    @GetMapping("/{supplierId}")
    public ResponseEntity<SupplierResponse> getSupplier(@PathVariable Long supplierId) {
        SupplierResponse supplier = supplierService.getSupplier(supplierId);
        return ResponseEntity.ok(supplier);
    }

    @GetMapping("/search")
    public ResponseEntity<List<SupplierResponse>> searchSuppliers(
            @RequestParam String q) {

        List<SupplierResponse> suppliers = supplierService.searchSuppliers(q);
        return ResponseEntity.ok(suppliers);
    }

    @GetMapping("/country/{country}")
    public ResponseEntity<List<SupplierResponse>> getSuppliersByCountry(
            @PathVariable String country) {

        List<SupplierResponse> suppliers = supplierService.getSuppliersByCountry(country);
        return ResponseEntity.ok(suppliers);
    }

    /**
     * Obtenir les meilleurs fournisseurs
     * GET /api/suppliers/top?limit=10
     */
    @GetMapping("/top")
    public ResponseEntity<List<SupplierResponse>> getTopSuppliers(
            @RequestParam(defaultValue = "10") int limit) {

        List<SupplierResponse> suppliers = supplierService.getTopSuppliers(limit);
        return ResponseEntity.ok(suppliers);
    }

    /**
     * Mettre à jour un fournisseur
     * PUT /api/suppliers/{id}
     */
    @PutMapping("/{supplierId}")
    public ResponseEntity<SupplierResponse> updateSupplier(
            @PathVariable Long supplierId,
            @Valid @RequestBody SupplierRequest request,
            Authentication authentication) {

        SupplierResponse response = supplierService.updateSupplier(supplierId, request);
        return ResponseEntity.ok(response);
    }

    /**
     * Changer le statut d'un fournisseur
     * PUT /api/suppliers/{id}/status?status=ACTIVE
     */
    @PutMapping("/{supplierId}/status")
    public ResponseEntity<SupplierResponse> updateSupplierStatus(
            @PathVariable Long supplierId,
            @RequestParam SupplierStatus status,
            Authentication authentication) {

        SupplierResponse response = supplierService.updateSupplierStatus(supplierId, status);
        return ResponseEntity.ok(response);
    }

    /**
     * Noter un fournisseur
     * POST /api/suppliers/{id}/rate
     */
    @PostMapping("/{supplierId}/rate")
    public ResponseEntity<SupplierResponse> rateSupplier(
            @PathVariable Long supplierId,
            @Valid @RequestBody SupplierRatingRequest request,
            Authentication authentication) {

        SupplierResponse response = supplierService.rateSupplier(supplierId, request);
        return ResponseEntity.ok(response);
    }

    /**
     * Obtenir les statistiques d'un fournisseur
     * GET /api/suppliers/{id}/statistics
     */
    @GetMapping("/{supplierId}/statistics")
    public ResponseEntity<SupplierStatisticsResponse> getSupplierStatistics(
            @PathVariable Long supplierId) {

        SupplierStatisticsResponse stats = supplierService.getSupplierStatistics(supplierId);
        return ResponseEntity.ok(stats);
    }

    /**
     * Obtenir le rapport de coûts par fournisseur
     * GET /api/suppliers/cost-report?startDate=2026-01-01&endDate=2026-12-31
     */
    @GetMapping("/cost-report")
    public ResponseEntity<List<SupplierCostReport>> getCostReport(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

        List<SupplierCostReport> report = supplierService.getCostReport(startDate, endDate);
        return ResponseEntity.ok(report);
    }
}
