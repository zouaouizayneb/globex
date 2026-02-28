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
public class TopSellingItem {
    private Long productId;
    private String productName;
    private Integer unitsSold;
    private Integer currentStock;
    private BigDecimal revenue;
}
