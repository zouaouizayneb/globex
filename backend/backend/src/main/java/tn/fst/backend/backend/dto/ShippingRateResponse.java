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
public class ShippingRateResponse {
    private String method;
    private BigDecimal cost;
    private String carrier;
    private Integer estimatedDays;
    private String description;
}
