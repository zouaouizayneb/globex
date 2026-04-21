package tn.fst.backend.backend.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import tn.fst.backend.backend.dto.*;
import tn.fst.backend.backend.service.InventoryService;

import java.util.List;

@RestController
@RequestMapping("/api/inventory")
@CrossOrigin(origins = "http://localhost:4200")
@RequiredArgsConstructor
public class InventoryController {

    private final InventoryService inventoryService;

    @GetMapping("/stock/{variantId}")
    public ResponseEntity<StockLevelResponse> getStockLevel(@PathVariable Long variantId) {
        StockLevelResponse response = inventoryService.getStockLevel(variantId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/alerts/low-stock")
    public ResponseEntity<List<LowStockAlert>> getLowStockAlerts() {
        List<LowStockAlert> alerts = inventoryService.getLowStockAlerts();
        return ResponseEntity.ok(alerts);
    }

    @GetMapping("/alerts/reorder")
    public ResponseEntity<List<ReorderAlert>> getReorderAlerts() {
        List<ReorderAlert> alerts = inventoryService.getReorderAlerts();
        return ResponseEntity.ok(alerts);
    }

    @GetMapping("/out-of-stock")
    public ResponseEntity<List<StockLevelResponse>> getOutOfStockVariants() {
        List<StockLevelResponse> outOfStock = inventoryService.getOutOfStockVariants();
        return ResponseEntity.ok(outOfStock);
    }

    @PostMapping("/adjust/{variantId}")
    public ResponseEntity<StockAdjustmentResponse> adjustStock(
            @PathVariable Long variantId,
            @Valid @RequestBody StockAdjustmentRequest request,
            Authentication authentication) {

        StockAdjustmentResponse response = inventoryService.adjustStock(variantId, request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/purchase/{variantId}")
    public ResponseEntity<StockAdjustmentResponse> recordPurchase(
            @PathVariable Long variantId,
            @Valid @RequestBody PurchaseRequest request,
            Authentication authentication) {

        StockAdjustmentResponse response = inventoryService.recordPurchase(
                variantId,
                request.getQuantity(),
                request.getSupplier()
        );
        return ResponseEntity.ok(response);
    }

    @GetMapping("/history/{variantId}")
    public ResponseEntity<List<StockMovementResponse>> getStockHistory(
            @PathVariable Long variantId) {

        List<StockMovementResponse> history = inventoryService.getStockHistory(variantId);
        return ResponseEntity.ok(history);
    }

    @GetMapping("/report")
    public ResponseEntity<InventoryReportResponse> getInventoryReport() {
        InventoryReportResponse report = inventoryService.getInventoryReport();
        return ResponseEntity.ok(report);
    }
}