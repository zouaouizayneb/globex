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
public class InvoiceSummaryResponse {
    private Long totalInvoices;
    private Long draftInvoices;
    private Long issuedInvoices;
    private Long paidInvoices;
    private Long overdueInvoices;
    private Long cancelledInvoices;
    private BigDecimal totalValue;
    private BigDecimal paidValue;
    private BigDecimal pendingValue;
    private BigDecimal overdueValue;
    private String currency;
}
