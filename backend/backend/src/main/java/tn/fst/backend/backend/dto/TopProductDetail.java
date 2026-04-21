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
public class TopProductDetail {
    private Long productId;
    private String productName;
    private String category;
    private Integer quantitySold;
    private BigDecimal revenue;
    private BigDecimal averagePrice;
}
