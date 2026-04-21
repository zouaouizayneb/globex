package tn.fst.backend.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

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
    private Long totalProducts;
    private Long totalClients;
    private Long totalCategories;
    private Long outOfStockProducts;
    private Long lowStockProducts;
    private List<TopProductItem> topProductsThisMonth;
    private List<CategorySalesData> topCategories;
    private Map<String, Long> ordersByStatus;
    private List<DailySalesBreakdown> recentSalesData;
    private String currency;
    private LocalDateTime generatedAt;
}
