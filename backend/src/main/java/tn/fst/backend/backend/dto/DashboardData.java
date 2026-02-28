package tn.fst.backend.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardData {
    private Long todayOrders;
    private BigDecimal todayRevenue;
    private Long monthOrders;
    private BigDecimal monthRevenue;
    private Long yearOrders;
    private BigDecimal yearRevenue;
    private List<TopProductItem> topProductsThisMonth;
    private List<DailySalesBreakdown> recentSalesData;
    private String currency;
    private LocalDateTime generatedAt;
}
