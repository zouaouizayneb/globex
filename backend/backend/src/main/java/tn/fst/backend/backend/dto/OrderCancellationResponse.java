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
public class OrderCancellationResponse {
    private Boolean success;
    private String message;
    private Boolean refundInitiated;
    private BigDecimal refundAmount;
}
