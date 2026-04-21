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
public class OrderItemSummary {
    private String productName;
    private String variantDetails;
    private Integer quantity;
    private BigDecimal unitPrice;
    private BigDecimal subtotal;
}
