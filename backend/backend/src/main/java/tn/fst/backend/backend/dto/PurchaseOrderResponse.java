package tn.fst.backend.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PurchaseOrderResponse {
    private Long purchaseOrderId;
    private String orderNumber;
    private Long supplierId;
    private String supplierName;
    private String status;
    private LocalDate orderDate;
    private LocalDate expectedDeliveryDate;
    private LocalDate actualDeliveryDate;
    private LocalDate paymentDueDate;
    private LocalDate paidDate;
    private BigDecimal subtotal;
    private BigDecimal taxAmount;
    private BigDecimal shippingCost;
    private BigDecimal discountAmount;
    private BigDecimal totalAmount;
    private String currency;
    private List<PurchaseOrderItemResponse> items;
    private String paymentMethod;
    private String paymentReference;
    private String shippingMethod;
    private String trackingNumber;
    private String warehouseLocation;
    private String notes;
    private String internalNotes;
    private Boolean isLate;
    private Boolean canBeModified;
    private Boolean canBeCancelled;
    private LocalDateTime createdAt;
}
