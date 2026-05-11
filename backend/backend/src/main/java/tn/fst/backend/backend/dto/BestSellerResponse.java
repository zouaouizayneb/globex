package tn.fst.backend.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BestSellerResponse {

    // Core product fields
    private Long idProduct;
    private String name;
    private String description;
    private BigDecimal price;
    private Double rating;
    private Integer stock;
    private String imageUrl;
    private CategoryResponse category;
    private List<ProductVariantResponse> variants;
    private List<ProductImageResponse> images;

    // Best-seller enrichment
    private Integer soldCount;       // total units sold across all orders
    private Integer soldPercentage;  // relative to the top seller (0-100)
    private Integer reviews;         // derived from rating count (approximated)
}
