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
public class FinancialStatisticsResponse {
    private BigDecimal totalRevenue;
    private BigDecimal totalTax;
    private BigDecimal pendingPayments;
    private Long totalInvoices;
    private Long paidInvoices;
    private Long overdueInvoices;
    private String currency;
    private LocalDate startDate;
    private LocalDate endDate;
}
