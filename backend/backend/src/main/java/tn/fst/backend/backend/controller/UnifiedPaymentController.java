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



@RestController
@RequestMapping("/api/payment")
@CrossOrigin(origins = "http://localhost:4200")
@RequiredArgsConstructor
public class UnifiedPaymentController {

    private final UnifiedPaymentService unifiedPaymentService;
    private final PayPalPaymentService paypalService;
    private final PaymentMethodService paymentMethodService;
    private final tn.fst.backend.backend.repository.OrderRepository orderRepository;
    private final tn.fst.backend.backend.repository.PaymentRepository paymentRepository;


    @GetMapping("/methods")
    public ResponseEntity<PaymentMethodsResponse> getPaymentMethods(
            @RequestParam String country) {

        PaymentMethodsResponse methods = paymentMethodService.getAvailablePaymentMethods(country);
        return ResponseEntity.ok(methods);
    }


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

    @PostMapping("/smt/callback")
    public ResponseEntity<String> handleSMTCallback(
            @RequestBody PaymentCallbackRequest callback) {

        try {
            String status = callback.getStatus();
            String orderId = callback.getOrderId();

            if ("SUCCESS".equals(status)) {
                // TODO: Mettre à jour la commande comme payée

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

    @PostMapping("/paypal/execute")
    public ResponseEntity<PayPalPaymentResponse> executePayPalPayment(
            @Valid @RequestBody PayPalExecuteRequest request) {

        try {
            PayPalPaymentResponse response = paypalService.executePayment(
                    request.getPaymentId(),
                    request.getPayerId()
            );

            if ("approved".equals(response.getStatus()) || "completed".equals(response.getStatus())) {
                tn.fst.backend.backend.entity.Payment p = paymentRepository.findByTransactionId(request.getPaymentId()).orElse(null);
                if (p == null) {
                    // Try by looking at all PENDING paypal payments if transactionId wasn't saved yet
                    p = paymentRepository.findByPaymentMethod(tn.fst.backend.backend.entity.PaymentMethod.PAYPAL).stream()
                            .filter(pmt -> pmt.getStatus() == tn.fst.backend.backend.entity.PaymentStatus.PENDING)
                            .findFirst().orElse(null);
                }

                if (p != null) {
                    p.setStatus(tn.fst.backend.backend.entity.PaymentStatus.COMPLETED);
                    paymentRepository.save(p);

                    tn.fst.backend.backend.entity.Order order = p.getOrder();
                    order.setStatus(tn.fst.backend.backend.entity.OrderStatus.CONFIRMED);
                    orderRepository.save(order);
                }
                
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


    @GetMapping("/currency")
    public ResponseEntity<CurrencyResponse> getCurrency(@RequestParam String country) {
        String currency = paymentMethodService.getCurrencyForCountry(country);
        return ResponseEntity.ok(new CurrencyResponse(country, currency));
    }

    private Long getCurrentUserId(Authentication authentication) {
        if (authentication != null && authentication.getPrincipal() instanceof User) {
            User user = (User) authentication.getPrincipal();
            return user.getIdUser();
        }
        return null;
    }

    public record CurrencyResponse(String country, String currency) {}
}