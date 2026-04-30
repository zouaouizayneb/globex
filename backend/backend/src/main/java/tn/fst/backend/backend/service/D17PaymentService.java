package tn.fst.backend.backend.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import tn.fst.backend.backend.dto.PaymentInitiateRequest;
import tn.fst.backend.backend.dto.PaymentInitiateResponse;

import java.util.UUID;

/**
 * Service de simulation de paiement D17.
 * D17 n'a pas d'API publique ouverte (nécessite un contrat direct La Poste ou un agrégateur).
 * Ce service génère une URL de simulation pour les besoins de démonstration.
 */
@Service
@Slf4j
public class D17PaymentService {

    public PaymentInitiateResponse createPayment(PaymentInitiateRequest request) {
        // Générer un ID de transaction unique
        String transactionId = "D17-" + UUID.randomUUID().toString().substring(0, 12).toUpperCase();
        
        // On renvoie directement l'URL de 'success' en y ajoutant le paramètre de simulation
        // Dans une vraie app, on renverrait vers une page de l'agrégateur ou on utiliserait un push SDK.
        String simulationLink = request.getReturnUrl() + "?method=D17&payment_id=" + transactionId;

        return PaymentInitiateResponse.builder()
                .paymentUrl(simulationLink)
                .transactionId(transactionId)
                .status("PENDING")
                .message("Notification D17 envoyée sur votre smartphone.")
                .build();
    }

    public String verifyPayment(String transactionId) {
        // En mode simulation, on considère la transaction comme réussie si elle est passée par le callback
        return "SUCCESS";
    }
}
