package tn.fst.backend.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InventoryReportResponse {
    private Long totalVariants;
    private Long inStock;
    private Long outOfStock;
    private Long lowStock;
    private Long needsReorder;
    private Integer totalStockUnits;
    private LocalDateTime generatedAt;
}
