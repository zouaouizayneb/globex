package tn.fst.backend.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentDetails {
    private Long paymentId;
    private String paymentMethod;
    private String status;
    private BigDecimal amount;
    private String currency;
    private String transactionId;
}
