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
public class TaxBreakdown {
    private BigDecimal domesticSales;
    private BigDecimal domesticTax;
    private BigDecimal internationalSales;
    private BigDecimal internationalTax;
}
