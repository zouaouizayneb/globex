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
public class ProductSearchRequest {

    // Search keyword (searches in name and description)
    private String keyword;

    // Filter by category (includes subcategories)
    private Long categoryId;

    // Price range
    private BigDecimal minPrice;
    private BigDecimal maxPrice;

    // Stock filter
    private Boolean inStock; // true = only show in-stock products

    // Sorting
    @Builder.Default
    private String sortBy = "name"; // name, price, createdAt

    @Builder.Default
    private String sortDirection = "ASC"; // ASC or DESC

    // Pagination
    @Builder.Default
    private Integer page = 0;

    @Builder.Default
    private Integer size = 10;
}
