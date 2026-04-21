package tn.fst.backend.backend.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import tn.fst.backend.backend.dto.*;
import tn.fst.backend.backend.entity.User;
import tn.fst.backend.backend.service.PaymentMethodService;
import tn.fst.backend.backend.service.PayPalPaymentService;
import tn.fst.backend.backend.service.UnifiedPaymentService;

/**
 * Controller unifié pour tous les paiements
 * Tunisie (SMT) + International (PayPal)
 */
@RestController
@RequestMapping("/api/payment")
@CrossOrigin(origins = "http://localhost:4200")
@RequiredArgsConstructor
public class UnifiedPaymentController {

    private final UnifiedPaymentService unifiedPaymentService;
    private final PayPalPaymentService paypalService;
    private final PaymentMethodService paymentMethodService;

    /**
     * Obtenir les méthodes de paiement disponibles selon le pays
     *
     * GET /api/payment/methods?country=TN
     * GET /api/payment/methods?country=US
     */
    @GetMapping("/methods")
    public ResponseEntity<PaymentMethodsResponse> getPaymentMethods(
            @RequestParam String country) {

        PaymentMethodsResponse methods = paymentMethodService.getAvailablePaymentMethods(country);
        return ResponseEntity.ok(methods);
    }

    /**
     * Initier un paiement
     *
     * POST /api/payment/initiate
     *
     * Body (Tunisie):
     * {
     *   "orderId": "ORD-001",
     *   "amount": 150.00,
     *   "currency": "TND",
     *   "country": "TN",
     *   "paymentMethod": "SMT_MONETIQUE",
     *   "returnUrl": "https://globex.tn/success",
     *   "cancelUrl": "https://globex.tn/cancel"
     * }
     *
     * Body (International):
     * {
     *   "orderId": "ORD-002",
     *   "amount": 99.99,
     *   "currency": "USD",
     *   "country": "US",
     *   "paymentMethod": "PAYPAL",
     *   "returnUrl": "https://globex.tn/success",
     *   "cancelUrl": "https://globex.tn/cancel"
     * }
     */
    @PostMapping("/initiate")
    public ResponseEntity<PaymentInitiateResponse> initiatePayment(
            @Valid @RequestBody PaymentInitiateRequest request,
            Authentication authentication) {

        try {
            PaymentInitiateResponse response = unifiedPaymentService.initiatePayment(request);
            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            return ResponseEntity
                    .badRequest()
                    .body(PaymentInitiateResponse.builder()
                            .status("ERROR")
                            .message(e.getMessage())
                            .build());

        } catch (Exception e) {
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(PaymentInitiateResponse.builder()
                            .status("ERROR")
                            .message("Erreur lors de l'initiation du paiement: " + e.getMessage())
                            .build());
        }
    }

    /**
     * Callback SMT après paiement
     *
     * POST /api/payment/smt/callback
     *
     * Appelé par SMT après que le client ait payé
     */
    @PostMapping("/smt/callback")
    public ResponseEntity<String> handleSMTCallback(
            @RequestBody PaymentCallbackRequest callback) {

        try {
            String status = callback.getStatus();
            String orderId = callback.getOrderId();

            if ("SUCCESS".equals(status)) {
                // TODO: Mettre à jour la commande comme payée
                // orderService.updateOrderStatus(orderId, OrderStatus.PAID);

                return ResponseEntity.ok("Payment successful");
            } else {
                return ResponseEntity.ok("Payment failed");
            }

        } catch (Exception e) {
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error processing callback: " + e.getMessage());
        }
    }

    /**
     * Exécuter un paiement PayPal après approbation
     *
     * POST /api/payment/paypal/execute
     *
     * Appelé après que le client ait approuvé le paiement sur PayPal
     *
     * Body:
     * {
     *   "paymentId": "PAYID-xxx",
     *   "payerId": "PAYERID-yyy"
     * }
     */
    @PostMapping("/paypal/execute")
    public ResponseEntity<PayPalPaymentResponse> executePayPalPayment(
            @Valid @RequestBody PayPalExecuteRequest request) {

        try {
            PayPalPaymentResponse response = paypalService.executePayment(
                    request.getPaymentId(),
                    request.getPayerId()
            );

            if ("approved".equals(response.getStatus()) || "completed".equals(response.getStatus())) {
                // TODO: Mettre à jour la commande
                return ResponseEntity.ok(response);
            } else {
                return ResponseEntity
                        .badRequest()
                        .body(response);
            }

        } catch (Exception e) {
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(PayPalPaymentResponse.builder()
                            .status("ERROR")
                            .message("Erreur lors de l'exécution du paiement: " + e.getMessage())
                            .build());
        }
    }

    /**
     * Vérifier le statut d'un paiement
     *
     * GET /api/payment/status/{transactionId}?method=SMT_MONETIQUE
     * GET /api/payment/status/{transactionId}?method=PAYPAL
     */
    @GetMapping("/status/{transactionId}")
    public ResponseEntity<PaymentStatusResponse> checkPaymentStatus(
            @PathVariable String transactionId,
            @RequestParam String method) {

        String status = unifiedPaymentService.checkPaymentStatus(transactionId, method).toString();

        return ResponseEntity.ok(
                PaymentStatusResponse.builder()
                        .transactionId(transactionId)
                        .paymentMethod(method)
                        .status(status)
                        .message("Status retrieved successfully")
                        .build()
        );
    }

    /**
     * Obtenir la devise pour un pays
     *
     * GET /api/payment/currency?country=TN
     * GET /api/payment/currency?country=US
     */
    @GetMapping("/currency")
    public ResponseEntity<CurrencyResponse> getCurrency(@RequestParam String country) {
        String currency = paymentMethodService.getCurrencyForCountry(country);
        return ResponseEntity.ok(new CurrencyResponse(country, currency));
    }

    // Helper methods
    private Long getCurrentUserId(Authentication authentication) {
        if (authentication != null && authentication.getPrincipal() instanceof User) {
            User user = (User) authentication.getPrincipal();
            return user.getIdUser();
        }
        return null;
    }

    // Inner class for currency response
    public record CurrencyResponse(String country, String currency) {}
}