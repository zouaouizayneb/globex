package tn.fst.backend.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;


@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InvoiceResponse {
    private Long invoiceId;
    private String invoiceNumber;
    private Long orderId;
    private String orderNumber;
    private Long userId;
    private String customerName;
    private LocalDate issueDate;
    private LocalDate dueDate;
    private LocalDate paidDate;
    private BigDecimal subtotal;
    private BigDecimal taxAmount;
    private BigDecimal taxRate;
    private String taxType;
    private BigDecimal shippingCost;
    private BigDecimal discountAmount;
    private BigDecimal totalAmount;
    private String currency;
    private String billingAddress;
    private String shippingAddress;
    private String status;
    private String paymentMethod;
    private String paymentReference;
    private String pdfPath;
    private Boolean pdfGenerated;
    private Boolean isOverdue;
    private Long daysOverdue;
    private String notes;
    private LocalDateTime createdAt;
}



