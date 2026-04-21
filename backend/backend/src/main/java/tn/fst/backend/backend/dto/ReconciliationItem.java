package tn.fst.backend.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReconciliationItem {
    private String transactionId;
    private LocalDate transactionDate;
    private BigDecimal amount;
    private String paymentMethod;
    private String status;
    private Long invoiceId;
    private String invoiceNumber;
    private Boolean isReconciled;
    private String reconciliationNotes;
}
