package tn.fst.backend.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LowStockItem {
    private Long variantId;
    private String productName;
    private String sku;
    private Integer currentStock;
    private Integer reorderPoint;
    private String status;
}
