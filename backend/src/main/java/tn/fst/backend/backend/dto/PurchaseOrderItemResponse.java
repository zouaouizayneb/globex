package tn.fst.backend.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PurchaseOrderItemResponse {
    private Long itemId;
    private Long variantId;
    private String productName;
    private String sku;
    private String color;
    private String size;
    private Integer quantity;
    private BigDecimal unitCost;
    private BigDecimal lineTotal;
    private Integer receivedQuantity;
    private Integer damagedQuantity;
    private Integer remainingQuantity;
    private String notes;
}
