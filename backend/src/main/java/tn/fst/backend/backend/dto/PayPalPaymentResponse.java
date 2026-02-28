package tn.fst.backend.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PayPalPaymentResponse {
    private String paymentId;       // ID du paiement PayPal
    private String approvalUrl;     // URL pour approuver le paiement
    private String status;          // created, approved, completed, failed
    private String message;
}
