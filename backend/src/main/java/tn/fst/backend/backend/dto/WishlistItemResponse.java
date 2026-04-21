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
public class WishlistItemResponse {
    private Long wishlistItemId;
    private Long variantId;
    private String sku;
    private String productName;
    private String variantDetails;
    private String imageUrl;
    private BigDecimal currentPrice;
    private Boolean inStock;
    private Integer availableStock;
    private String addedAt;
}
