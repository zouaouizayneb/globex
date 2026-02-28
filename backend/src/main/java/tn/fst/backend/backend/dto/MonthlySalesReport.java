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
public class MonthlySalesReport {
    private Integer year;
    private Integer month;
    private Long totalOrders;
    private BigDecimal totalRevenue;
    private BigDecimal totalProfit;
    private BigDecimal averageOrderValue;
    private List<TopProductItem> topProducts;
    private List<PaymentMethodSales> salesByPaymentMethod;
    private List<DailySalesBreakdown> dailyBreakdown;
    private String currency;
}
