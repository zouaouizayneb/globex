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
public class SupplierStatisticsResponse {
    private Long supplierId;
    private String supplierName;
    private Long totalOrders;
    private Long completedOrders;
    private Long onTimeDeliveries;
    private Long lateDeliveries;
    private BigDecimal onTimeDeliveryRate;
    private BigDecimal totalSpent;
    private BigDecimal rating;
}
