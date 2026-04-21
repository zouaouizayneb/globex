package tn.fst.backend.backend.dto;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class FrontendOrderItem {
    private Long productId;
    private String productName;
    private Integer quantity;
    private BigDecimal unitPrice;
}
