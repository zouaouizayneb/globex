package tn.fst.backend.backend.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import tn.fst.backend.backend.dto.*;
import tn.fst.backend.backend.entity.PurchaseOrderStatus;
import tn.fst.backend.backend.service.PurchaseOrderService;

import java.util.List;


@RestController
@RequestMapping("/api/purchase-orders")
@CrossOrigin(origins = "http://localhost:4200")
@RequiredArgsConstructor
public class PurchaseOrderController {

    private final PurchaseOrderService purchaseOrderService;

    @PostMapping
    public ResponseEntity<PurchaseOrderResponse> createPurchaseOrder(
            @Valid @RequestBody PurchaseOrderRequest request,
            Authentication authentication) {

        PurchaseOrderResponse response = purchaseOrderService.createPurchaseOrder(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<PurchaseOrderResponse>> getAllPurchaseOrders() {
        List<PurchaseOrderResponse> orders = purchaseOrderService.getAllPurchaseOrders();
        return ResponseEntity.ok(orders);
    }

    @GetMapping("/{purchaseOrderId}")
    public ResponseEntity<PurchaseOrderResponse> getPurchaseOrder(
            @PathVariable Long purchaseOrderId) {

        PurchaseOrderResponse order = purchaseOrderService.getPurchaseOrder(purchaseOrderId);
        return ResponseEntity.ok(order);
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<List<PurchaseOrderResponse>> getPurchaseOrdersByStatus(
            @PathVariable PurchaseOrderStatus status) {

        List<PurchaseOrderResponse> orders = purchaseOrderService.getPurchaseOrdersByStatus(status);
        return ResponseEntity.ok(orders);
    }

    @GetMapping("/supplier/{supplierId}")
    public ResponseEntity<List<PurchaseOrderResponse>> getPurchaseOrdersBySupplier(
            @PathVariable Long supplierId) {

        List<PurchaseOrderResponse> orders = purchaseOrderService.getPurchaseOrdersBySupplier(supplierId);
        return ResponseEntity.ok(orders);
    }

    @GetMapping("/late")
    public ResponseEntity<List<PurchaseOrderResponse>> getLatePurchaseOrders() {
        List<PurchaseOrderResponse> orders = purchaseOrderService.getLatePurchaseOrders();
        return ResponseEntity.ok(orders);
    }

    /**
     * Mettre à jour un bon de commande
     * PUT /api/purchase-orders/{id}
     */
    @PutMapping("/{purchaseOrderId}")
    public ResponseEntity<PurchaseOrderResponse> updatePurchaseOrder(
            @PathVariable Long purchaseOrderId,
            @Valid @RequestBody PurchaseOrderRequest request,
            Authentication authentication) {

        PurchaseOrderResponse response = purchaseOrderService.updatePurchaseOrder(purchaseOrderId, request);
        return ResponseEntity.ok(response);
    }

    /**
     * Envoyer un bon de commande au fournisseur
     * POST /api/purchase-orders/{id}/submit
     */
    @PostMapping("/{purchaseOrderId}/submit")
    public ResponseEntity<PurchaseOrderResponse> submitPurchaseOrder(
            @PathVariable Long purchaseOrderId,
            Authentication authentication) {

        PurchaseOrderResponse response = purchaseOrderService.submitPurchaseOrder(purchaseOrderId);
        return ResponseEntity.ok(response);
    }

    /**
     * Confirmer un bon de commande
     * POST /api/purchase-orders/{id}/confirm
     */
    @PostMapping("/{purchaseOrderId}/confirm")
    public ResponseEntity<PurchaseOrderResponse> confirmPurchaseOrder(
            @PathVariable Long purchaseOrderId,
            Authentication authentication) {

        PurchaseOrderResponse response = purchaseOrderService.confirmPurchaseOrder(purchaseOrderId);
        return ResponseEntity.ok(response);
    }

    /**
     * Marquer comme expédié
     * POST /api/purchase-orders/{id}/ship
     */
    @PostMapping("/{purchaseOrderId}/ship")
    public ResponseEntity<PurchaseOrderResponse> markAsShipped(
            @PathVariable Long purchaseOrderId,
            @RequestParam String trackingNumber,
            Authentication authentication) {

        PurchaseOrderResponse response = purchaseOrderService.markAsShipped(purchaseOrderId, trackingNumber);
        return ResponseEntity.ok(response);
    }

    /**
     * Recevoir un bon de commande
     * POST /api/purchase-orders/{id}/receive
     */
    @PostMapping("/{purchaseOrderId}/receive")
    public ResponseEntity<PurchaseOrderResponse> receivePurchaseOrder(
            @PathVariable Long purchaseOrderId,
            @Valid @RequestBody ReceivePurchaseOrderRequest request,
            Authentication authentication) {

        PurchaseOrderResponse response = purchaseOrderService.receivePurchaseOrder(purchaseOrderId, request);
        return ResponseEntity.ok(response);
    }

    /**
     * Marquer comme payé
     * POST /api/purchase-orders/{id}/pay
     */
    @PostMapping("/{purchaseOrderId}/pay")
    public ResponseEntity<PurchaseOrderResponse> markAsPaid(
            @PathVariable Long purchaseOrderId,
            @Valid @RequestBody PaymentInfoRequest request,
            Authentication authentication) {

        PurchaseOrderResponse response = purchaseOrderService.markAsPaid(purchaseOrderId, request);
        return ResponseEntity.ok(response);
    }

    /**
     * Annuler un bon de commande
     * POST /api/purchase-orders/{id}/cancel
     */
    @PostMapping("/{purchaseOrderId}/cancel")
    public ResponseEntity<PurchaseOrderResponse> cancelPurchaseOrder(
            @PathVariable Long purchaseOrderId,
            @RequestParam String reason,
            Authentication authentication) {

        PurchaseOrderResponse response = purchaseOrderService.cancelPurchaseOrder(purchaseOrderId, reason);
        return ResponseEntity.ok(response);
    }
}
