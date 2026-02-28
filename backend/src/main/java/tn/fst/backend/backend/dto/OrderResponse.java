package tn.fst.backend.backend.dto;


import lombok.Builder;
import lombok.Data;
import tn.fst.backend.backend.entity.OrderStatus;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
@Builder
public class OrderResponse {
    private Long orderId;
    private Long clientId;
    private String clientName;
    private LocalDate orderDate;
    private OrderStatus status;
    private BigDecimal totalAmount;
    private String invoiceNumber;
    private List<OrderItemResponse> items;
}