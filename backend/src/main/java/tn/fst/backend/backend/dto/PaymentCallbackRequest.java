package tn.fst.backend.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PaymentCallbackRequest {
    private String transactionId;
    private String status;
    private String signature;
    private String orderId;
    private String amount;
}
