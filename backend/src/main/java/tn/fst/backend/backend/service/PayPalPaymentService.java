package tn.fst.backend.backend.service;

import com.paypal.api.payments.*;
import com.paypal.base.rest.APIContext;
import com.paypal.base.rest.PayPalRESTException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import tn.fst.backend.backend.dto.PayPalPaymentRequest;
import tn.fst.backend.backend.dto.PayPalPaymentResponse;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

/**
 * Service de paiement PayPal
 * Pour les clients internationaux
 */
@Service
@RequiredArgsConstructor
public class PayPalPaymentService {

    @Value("${paypal.client.id}")
    private String clientId;

    @Value("${paypal.client.secret}")
    private String clientSecret;

    @Value("${paypal.mode:sandbox}")
    private String mode; // sandbox ou live

    /**
     * Créer un paiement PayPal
     */
    public PayPalPaymentResponse createPayment(PayPalPaymentRequest request) {

        try {
            // Créer le contexte API PayPal
            APIContext apiContext = new APIContext(clientId, clientSecret, mode);

            // Configurer le montant
            Amount amount = new Amount();
            amount.setCurrency(request.getCurrency());
            amount.setTotal(formatAmount(request.getAmount()));

            // Configurer la transaction
            Transaction transaction = new Transaction();
            transaction.setDescription(request.getDescription());
            transaction.setAmount(amount);

            List<Transaction> transactions = new ArrayList<>();
            transactions.add(transaction);

            // Configurer le payeur
            Payer payer = new Payer();
            payer.setPaymentMethod("paypal");

            // Configurer le paiement
            Payment payment = new Payment();
            payment.setIntent("sale"); // Paiement immédiat
            payment.setPayer(payer);
            payment.setTransactions(transactions);

            // URLs de redirection
            RedirectUrls redirectUrls = new RedirectUrls();
            redirectUrls.setCancelUrl(request.getCancelUrl());
            redirectUrls.setReturnUrl(request.getReturnUrl());
            payment.setRedirectUrls(redirectUrls);

            // Créer le paiement via l'API PayPal
            Payment createdPayment = payment.create(apiContext);

            // Extraire l'URL d'approbation
            String approvalUrl = null;
            for (Links link : createdPayment.getLinks()) {
                if (link.getRel().equals("approval_url")) {
                    approvalUrl = link.getHref();
                    break;
                }
            }

            return PayPalPaymentResponse.builder()
                    .paymentId(createdPayment.getId())
                    .approvalUrl(approvalUrl)
                    .status(createdPayment.getState())
                    .message("Paiement PayPal créé avec succès")
                    .build();

        } catch (PayPalRESTException e) {
            throw new RuntimeException("Erreur lors de la création du paiement PayPal: " + e.getMessage(), e);
        }
    }

    /**
     * Exécuter un paiement après approbation du client
     */
    public PayPalPaymentResponse executePayment(String paymentId, String payerId) {

        try {
            APIContext apiContext = new APIContext(clientId, clientSecret, mode);

            Payment payment = new Payment();
            payment.setId(paymentId);

            PaymentExecution paymentExecution = new PaymentExecution();
            paymentExecution.setPayerId(payerId);

            Payment executedPayment = payment.execute(apiContext, paymentExecution);

            return PayPalPaymentResponse.builder()
                    .paymentId(executedPayment.getId())
                    .status(executedPayment.getState())
                    .message("Paiement exécuté avec succès")
                    .build();

        } catch (PayPalRESTException e) {
            throw new RuntimeException("Erreur lors de l'exécution du paiement PayPal: " + e.getMessage(), e);
        }
    }

    /**
     * Vérifier le statut d'un paiement
     */
    public String verifyPayment(String paymentId) {

        try {
            APIContext apiContext = new APIContext(clientId, clientSecret, mode);
            Payment payment = Payment.get(apiContext, paymentId);

            String state = payment.getState();

            // Mapper les états PayPal vers nos états
            switch (state) {
                case "approved":
                case "completed":
                    return "SUCCESS";
                case "failed":
                    return "FAILED";
                case "cancelled":
                    return "CANCELLED";
                default:
                    return "PENDING";
            }

        } catch (PayPalRESTException e) {
            return "FAILED";
        }
    }

    /**
     * Rembourser un paiement
     */
    public boolean refundPayment(String saleId) {

        try {
            APIContext apiContext = new APIContext(clientId, clientSecret, mode);

            Sale sale = new Sale();
            sale.setId(saleId);

            RefundRequest refundRequest = new RefundRequest();
            Refund refund = sale.refund(apiContext, refundRequest);

            return "completed".equals(refund.getState());

        } catch (PayPalRESTException e) {
            return false;
        }
    }

    /**
     * Formater le montant (2 décimales)
     */
    private String formatAmount(BigDecimal amount) {
        return amount.setScale(2, RoundingMode.HALF_UP).toString();
    }
}
