package tn.fst.backend.backend.dto;

import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;

@Data
@Builder
public class OrderStatistics {
    private Long totalOrders;
    private BigDecimal totalRevenue;
    private Long pendingOrders;
    private Long completedOrders;
    private Long cancelledOrders;
}