package tn.fst.backend.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class YearlySalesReport {
    private Integer year;
    private Long totalOrders;
    private BigDecimal totalRevenue;
    private BigDecimal totalProfit;
    private BigDecimal averageOrderValue;
    private List<TopProductItem> topProducts;
    private List<PaymentMethodSales> salesByPaymentMethod;
    private List<MonthlySalesBreakdown> monthlyBreakdown;
    private String currency;
}
