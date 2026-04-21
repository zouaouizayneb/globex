package tn.fst.backend.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentInitiateResponse {
    private String paymentUrl;         // Pour SMT (redirection)
    private String clientSecret;       // Pour Stripe
    private String paymentIntentId;    // Pour Stripe
    private String transactionId;      // ID transaction
    private String paymentMethod;      // Méthode utilisée
    private String status;            // PENDING, SUCCESS, FAILED
    private String message;
    private String currency;
}
