package tn.fst.backend.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReorderAlert {
    private Long variantId;
    private String productName;
    private String sku;
    private String color;
    private String size;
    private Integer currentStock;
    private Integer reorderPoint;
    private Integer suggestedOrderQuantity;
    private String priority; // HIGH, MEDIUM, LOW
}
