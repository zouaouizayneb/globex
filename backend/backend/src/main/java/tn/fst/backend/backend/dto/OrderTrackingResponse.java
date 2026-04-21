package tn.fst.backend.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderTrackingResponse {
    private Long orderId;
    private String orderNumber;
    private String status;
    private LocalDate orderDate;
    private BigDecimal totalAmount;
    private String paymentStatus;
    private String shipmentStatus;
    private String trackingNumber;
    private LocalDate estimatedDelivery;
    private Boolean canCancel;
    private Boolean canReturn;
}
