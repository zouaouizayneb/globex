package tn.fst.backend.backend.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import tn.fst.backend.backend.dto.*;
import tn.fst.backend.backend.entity.User;
import tn.fst.backend.backend.service.*;

import java.util.List;

/**
 * Controller pour le processus de checkout
 * Version corrigée utilisant UnifiedPaymentService (SMT + PayPal)
 */
@RestController
@RequestMapping("/api/checkout")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class CheckoutController {

    private final CheckoutService checkoutService;
    private final UnifiedPaymentService unifiedPaymentService;  // ← Utiliser UnifiedPaymentService
    private final ShippingService shippingService;
    private final TaxService taxService;

    /**
     * Get checkout summary (before placing order)
     * GET /api/checkout/summary?shippingMethod=STANDARD&country=US
     */
    @GetMapping("/summary")
    public ResponseEntity<OrderSummary> getCheckoutSummary(
            @RequestParam String shippingMethod,
            @RequestParam String country,
            Authentication authentication) {

        Long userId = getCurrentUserId(authentication);
        OrderSummary summary = checkoutService.getCheckoutSummary(userId, shippingMethod, country);
        return ResponseEntity.ok(summary);
    }

    /**
     * Get available shipping rates for a country
     * GET /api/checkout/shipping-rates?country=US
     */
    @GetMapping("/shipping-rates")
    public ResponseEntity<List<ShippingRateResponse>> getShippingRates(
            @RequestParam String country) {

        List<ShippingRateResponse> rates = shippingService.getAvailableShippingRates(country);
        return ResponseEntity.ok(rates);
    }

    /**
     * Calculate shipping cost
     * POST /api/checkout/calculate-shipping
     */
    @PostMapping("/calculate-shipping")
    public ResponseEntity<ShippingRateResponse> calculateShipping(
            @Valid @RequestBody ShippingRateRequest request) {

        ShippingRateResponse rate = shippingService.calculateShippingCost(request);
        return ResponseEntity.ok(rate);
    }

    /**
     * Calculate tax
     * POST /api/checkout/calculate-tax
     */
    @PostMapping("/calculate-tax")
    public ResponseEntity<TaxCalculationResponse> calculateTax(
            @Valid @RequestBody TaxCalculationRequest request) {

        TaxCalculationResponse tax = taxService.calculateTax(request);
        return ResponseEntity.ok(tax);
    }

    /**
     * SUPPRIMÉ: Create Stripe payment intent
     * (Vous utilisez PayPal maintenant, pas Stripe)
     *
     * Pour créer un paiement, utilisez:
     * POST /api/payment/initiate (dans UnifiedPaymentController)
     */

    /**
     * Process complete checkout
     * POST /api/checkout
     */
    @PostMapping
    public ResponseEntity<CheckoutResponse> processCheckout(
            @Valid @RequestBody CheckoutRequest request,
            Authentication authentication) {

        Long userId = getCurrentUserId(authentication);
        CheckoutResponse response = checkoutService.processCheckout(userId, request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    // Helper to get current user ID
    private Long getCurrentUserId(Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        return user.getIdUser();
    }
}