package tn.fst.backend.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StockAdjustmentResponse {
    private Boolean success;
    private String message;
    private Long variantId;
    private Integer stockBefore;
    private Integer stockAfter;
    private Integer quantityChanged;
}
