package tn.fst.backend.backend.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import tn.fst.backend.backend.dto.SMTPaymentRequest;
import tn.fst.backend.backend.dto.SMTPaymentResponse;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Service de paiement SMT Monétique (VERSION SIMULÉE pour tests)
 * En production, remplacer par de vrais appels à l'API SMT
 */
@Service
@RequiredArgsConstructor
public class SMTPaymentService {

    // Configuration pour simulation
    private static final String MERCHANT_ID = "TEST_MERCHANT_001";
    private static final String TERMINAL_ID = "TEST_TERMINAL_001";
    private static final String API_KEY = "test_smt_api_key_123";
    private static final String SANDBOX_URL = "https://sandbox.monetique.tn";

    /**
     * Créer une transaction de paiement SMT
     */
    public SMTPaymentResponse createPayment(SMTPaymentRequest request) {

        // Générer un ID de transaction unique
        String transactionId = "SMT-" + UUID.randomUUID().toString().substring(0, 12).toUpperCase();

        // Construire les paramètres
        Map<String, String> params = buildPaymentParams(request, transactionId);

        // Générer la signature de sécurité
        String signature = generateSignature(params);
        params.put("signature", signature);

        // Construire l'URL de paiement
        String paymentUrl = buildPaymentUrl(params);

        // En mode SIMULATION: Retourner directement l'URL
        // En production: Faire un appel HTTP à l'API SMT et parser la réponse

        return SMTPaymentResponse.builder()
                .paymentUrl(paymentUrl)
                .transactionId(transactionId)
                .status("PENDING")
                .message("Paiement initié avec succès")
                .build();
    }

    /**
     * Vérifier le statut d'un paiement
     */
    public String verifyPayment(String transactionId) {
        // En mode simulation: retourner SUCCESS
        // En production: Appeler l'API SMT pour vérifier le statut réel
        return "SUCCESS";
    }

    /**
     * Traiter le callback de SMT (après paiement)
     */
    public boolean handleCallback(String transactionId, String status, String signature) {
        Map<String, String> params = new HashMap<>();
        params.put("transaction_id", transactionId);
        params.put("status", status);

        String expectedSignature = generateSignature(params);
        return expectedSignature.equals(signature);
    }

    /**
     * Construire les paramètres de paiement
     */
    private Map<String, String> buildPaymentParams(SMTPaymentRequest request, String transactionId) {
        Map<String, String> params = new HashMap<>();
        params.put("merchant_id", MERCHANT_ID);
        params.put("terminal_id", TERMINAL_ID);
        params.put("transaction_id", transactionId);
        params.put("amount", formatAmount(request.getAmount()));
        params.put("currency", "TND");
        params.put("order_id", request.getOrderId());
        params.put("return_url", request.getReturnUrl());
        params.put("cancel_url", request.getCancelUrl());
        params.put("description", request.getDescription() != null ? request.getDescription() : "Paiement commande");
        return params;
    }

    /**
     * Générer la signature de sécurité (SHA-256)
     */
    private String generateSignature(Map<String, String> params) {
        String data = params.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .map(e -> e.getKey() + "=" + e.getValue())
                .collect(Collectors.joining("&"));

        data += "&key=" + API_KEY;

        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(data.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Erreur lors de la génération de la signature", e);
        }
    }

    /**
     * Construire l'URL de paiement avec paramètres
     */
    private String buildPaymentUrl(Map<String, String> params) {
        String queryString = params.entrySet().stream()
                .map(e -> e.getKey() + "=" + urlEncode(e.getValue()))
                .collect(Collectors.joining("&"));

        return SANDBOX_URL + "/payment?" + queryString;
    }

    /**
     * Formater le montant pour SMT (millimes)
     * Exemple: 150.50 TND → 150500 millimes
     */
    private String formatAmount(BigDecimal amount) {
        // SMT attend le montant en millimes (1 TND = 1000 millimes)
        return String.valueOf(amount.multiply(BigDecimal.valueOf(1000)).longValue());
    }

    /**
     * Encoder une valeur pour URL
     */
    private String urlEncode(String value) {
        try {
            return java.net.URLEncoder.encode(value, StandardCharsets.UTF_8.toString());
        } catch (Exception e) {
            return value;
        }
    }
}