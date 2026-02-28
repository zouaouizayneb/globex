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
public class TaxCalculationResponse {
    private BigDecimal taxAmount;
    private BigDecimal taxRate;
    private String taxType; // VAT, GST, Sales Tax
}
