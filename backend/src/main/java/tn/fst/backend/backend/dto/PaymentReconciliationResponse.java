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
public class PaymentReconciliationResponse {
    private Long totalTransactions;
    private Long reconciledTransactions;
    private Long unreconciledTransactions;
    private BigDecimal totalReconciledAmount;
    private BigDecimal totalUnreconciledAmount;
    private String currency;
}
