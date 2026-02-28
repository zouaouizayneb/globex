package tn.fst.backend.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;


@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DailySalesReport {
    private LocalDate startDate;
    private LocalDate endDate;
    private String period;
    private Long totalOrders;
    private BigDecimal totalRevenue;
    private BigDecimal totalProfit;
    private BigDecimal averageOrderValue;
    private List<TopProductItem> topProducts;
    private List<PaymentMethodSales> salesByPaymentMethod;
    private String currency;
    private LocalDateTime generatedAt;
}



