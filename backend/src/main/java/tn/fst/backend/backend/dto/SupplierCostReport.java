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
public class SupplierCostReport {
    private Long supplierId;
    private String supplierName;
    private Integer orderCount;
    private BigDecimal totalCost;
    private String currency;
}
