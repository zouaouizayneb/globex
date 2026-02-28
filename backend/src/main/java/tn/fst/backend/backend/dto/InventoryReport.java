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
public class InventoryReport {
    private Long totalProducts;
    private Long totalVariants;
    private Long inStockVariants;
    private Long outOfStockVariants;
    private Long lowStockVariants;
    private Integer totalStockUnits;
    private BigDecimal estimatedStockValue;
    private List<LowStockItem> lowStockItems;
    private List<TopSellingItem> topSellingItems;
    private LocalDateTime generatedAt;
}
