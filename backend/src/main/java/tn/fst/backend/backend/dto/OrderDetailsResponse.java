package tn.fst.backend.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderDetailsResponse {
    private Long orderId;
    private String orderNumber;
    private LocalDate orderDate;
    private String status;
    private List<OrderItemDetails> items;
    private BigDecimal subtotal;
    private BigDecimal shippingCost;
    private BigDecimal taxAmount;
    private BigDecimal totalAmount;
    private String paymentMethod;
    private String paymentStatus;
    private String shippingAddress;
    private String trackingNumber;
    private LocalDate estimatedDelivery;
    private Boolean canCancel;
    private Boolean canReturn;
}
