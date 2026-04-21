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
public class TopProductItem {
    private Long productId;
    private String productName;
    private Long variantId;
    private String sku;
    private Integer quantitySold;
    private BigDecimal revenue;
}
