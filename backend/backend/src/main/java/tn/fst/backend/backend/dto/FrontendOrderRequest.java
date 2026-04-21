package tn.fst.backend.backend.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.util.List;

@Data
public class FrontendOrderRequest {
    private String email;
    private String shippingAddress;
    private String country;
    private String shippingMethod;
    private String paymentMethod;
    private BigDecimal subtotal;
    private BigDecimal shippingCost;
    private BigDecimal taxes;
    private BigDecimal total;
    private List<FrontendOrderItem> items;
    private String status;
}
