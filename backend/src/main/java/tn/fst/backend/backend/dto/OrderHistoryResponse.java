package tn.fst.backend.backend.dto;

import jakarta.validation.constraints.NotBlank;
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
public class OrderHistoryResponse {
    private Long orderId;
    private String orderNumber;
    private LocalDate orderDate;
    private String status;
    private BigDecimal totalAmount;
    private Integer itemCount;
    private String paymentMethod;
    private String shippingStatus;
    private Boolean canCancel;
    private Boolean canReturn;
}

