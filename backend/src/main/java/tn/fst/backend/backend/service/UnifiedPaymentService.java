package tn.fst.backend.backend.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import tn.fst.backend.backend.dto.*;
import tn.fst.backend.backend.entity.PaymentMethod;

/**
 * Service unifié de paiement
 * Tunisie → SMT Monétique
 * International → PayPal
 */
@Service
@RequiredArgsConstructor
public class UnifiedPaymentService {

    private final SMTPaymentService smtService;
    private final PayPalPaymentService paypalService;

    /**
     * Initier un paiement selon le pays et la méthode choisie
     */
    public PaymentInitiateResponse initiatePayment(PaymentInitiateRequest request) {

        String country = request.getCountry();
        PaymentMethod method = PaymentMethod.valueOf(request.getPaymentMethod());

        // Clients TUNISIENS
        if ("TN".equalsIgnoreCase(country)) {
            switch (method) {
                case SMT_MONETIQUE:
                case E_DINAR:
                    return processSMTPayment(request);

                case CASH_ON_DELIVERY:
                    return processCODPayment(request);

                default:
                    throw new IllegalArgumentException("Méthode non supportée en Tunisie: " + method);
            }
        }
        // Clients INTERNATIONAUX
        else {
            if (method == PaymentMethod.PAYPAL) {
                return processPayPalPayment(request);
            } else {
                throw new IllegalArgumentException("Seul PayPal est supporté pour les paiements internationaux");
            }
        }
    }

    /**
     * Traiter paiement SMT (Tunisie)
     */
    private PaymentInitiateResponse processSMTPayment(PaymentInitiateRequest request) {

        SMTPaymentResponse smtResponse = smtService.createPayment(
                SMTPaymentRequest.builder()
                        .amount(request.getAmount())
                        .orderId(request.getOrderId())
                        .returnUrl(request.getReturnUrl() != null ? request.getReturnUrl() : "https://globex.tn/payment/success")
                        .cancelUrl(request.getCancelUrl() != null ? request.getCancelUrl() : "https://globex.tn/payment/cancel")
                        .description(request.getDescription())
                        .build()
        );

        return PaymentInitiateResponse.builder()
                .paymentUrl(smtResponse.getPaymentUrl())
                .transactionId(smtResponse.getTransactionId())
                .paymentMethod("SMT_MONETIQUE")
                .status("PENDING")
                .message("Redirection vers SMT Monétique pour paiement sécurisé")
                .currency("TND")
                .build();
    }

    /**
     * Traiter paiement PayPal (International)
     */
    private PaymentInitiateResponse processPayPalPayment(PaymentInitiateRequest request) {

        PayPalPaymentResponse paypalResponse = paypalService.createPayment(
                PayPalPaymentRequest.builder()
                        .amount(request.getAmount())
                        .currency(request.getCurrency())
                        .orderId(request.getOrderId())
                        .description(request.getDescription() != null ? request.getDescription() : "Order " + request.getOrderId())
                        .returnUrl(request.getReturnUrl() != null ? request.getReturnUrl() : "https://globex.tn/payment/success")
                        .cancelUrl(request.getCancelUrl() != null ? request.getCancelUrl() : "https://globex.tn/payment/cancel")
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

        if ("SMT_MONETIQUE".equals(paymentMethod)) {
            return smtService.verifyPayment(transactionId);
        }
        else if ("PAYPAL".equals(paymentMethod)) {
            return paypalService.verifyPayment(transactionId);
        }

        return "UNKNOWN";
    }
}