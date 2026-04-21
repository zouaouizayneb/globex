package tn.fst.backend.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CheckoutResponse {
    private Long orderId;
    private String orderNumber;
    private OrderSummary orderSummary;
    private PaymentDetails paymentDetails;
    private ShippingDetails shippingDetails;
    private String message;
}
