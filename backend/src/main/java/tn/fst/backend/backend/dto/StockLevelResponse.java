package tn.fst.backend.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;


@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StockLevelResponse {
    private Long variantId;
    private String productName;
    private String sku;
    private String color;
    private String size;
    private Integer currentStock;
    private Integer lowStockThreshold;
    private Integer reorderPoint;
    private Boolean isLowStock;
    private Boolean needsReorder;
    private String status; // IN_STOCK, LOW_STOCK, OUT_OF_STOCK, REORDER_NEEDED
}


