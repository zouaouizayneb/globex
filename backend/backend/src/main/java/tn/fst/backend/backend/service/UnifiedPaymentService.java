package tn.fst.backend.backend.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import tn.fst.backend.backend.dto.*;
import tn.fst.backend.backend.entity.PaymentMethod;

/**
 * Service unifié de paiement
 * Tunisie → PayPal ou Cash on Delivery
 * International → PayPal
 */
@Service
@RequiredArgsConstructor
public class UnifiedPaymentService {

    private final PayPalPaymentService paypalService;

    /**
     * Initier un paiement selon le pays et la méthode choisie
     */
    public PaymentInitiateResponse initiatePayment(PaymentInitiateRequest request) {

        String country = request.getCountry();
        PaymentMethod method = PaymentMethod.valueOf(request.getPaymentMethod());

        // Support PayPal for all countries
        if (method == PaymentMethod.PAYPAL) {
            return processPayPalPayment(request);
        }
        // Support Cash on Delivery for Tunisia only
        else if (method == PaymentMethod.CASH_ON_DELIVERY && "TN".equalsIgnoreCase(country)) {
            return processCODPayment(request);
        }
        else {
            throw new IllegalArgumentException("Méthode de paiement non supportée: " + method);
        }
    }

    /**
     * Traiter paiement PayPal (International & Tunisia)
     */
    private PaymentInitiateResponse processPayPalPayment(PaymentInitiateRequest request) {

        PayPalPaymentResponse paypalResponse = paypalService.createPayment(
                PayPalPaymentRequest.builder()
                        .amount(request.getAmount())
                        .currency(request.getCurrency())
                        .orderId(request.getOrderId())
                        .description(request.getDescription() != null ? request.getDescription() : "Order " + request.getOrderId())
                        .returnUrl(request.getReturnUrl() != null ? request.getReturnUrl() : "http://localhost:4200/payment-result")
                        .cancelUrl(request.getCancelUrl() != null ? request.getCancelUrl() : "http://localhost:4200/payment-result")
                        .build()
        );

        return PaymentInitiateResponse.builder()
                .paymentUrl(paypalResponse.getApprovalUrl())
                .transactionId(paypalResponse.getPaymentId())
                .paymentMethod("PAYPAL")
                .status("PENDING")
                .message("Redirection vers PayPal pour paiement sécurisé")
                .currency(request.getCurrency())
                .build();
    }

    /**
     * Traiter paiement à la livraison (Tunisie uniquement)
     */
    private PaymentInitiateResponse processCODPayment(PaymentInitiateRequest request) {

        return PaymentInitiateResponse.builder()
                .paymentMethod("CASH_ON_DELIVERY")
                .status("PENDING")
                .message("Commande confirmée. Paiement à la livraison.")
                .currency("TND")
                .build();
    }

    /**
     * Vérifier le statut d'un paiement
     */
    public Object checkPaymentStatus(String transactionId, String paymentMethod) {

        if ("PAYPAL".equals(paymentMethod)) {
            return paypalService.verifyPayment(transactionId);
        }
        else if ("CASH_ON_DELIVERY".equals(paymentMethod)) {
            return PaymentInitiateResponse.builder()
                    .paymentMethod("CASH_ON_DELIVERY")
                    .status("PENDING")
                    .message("Paiement à la livraison")
                    .build();
        }

        return "UNKNOWN";
    }
}