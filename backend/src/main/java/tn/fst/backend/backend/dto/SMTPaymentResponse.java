package tn.fst.backend.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SMTPaymentResponse {
    private String paymentUrl;      // URL de redirection vers SMT
    private String transactionId;   // ID transaction SMT
    private String status;          // PENDING, SUCCESS, FAILED
    private String message;
}
