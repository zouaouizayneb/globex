package tn.fst.backend.backend.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ShippingRateRequest {
    @NotBlank(message = "Country is required")
    private String destinationCountry;

    private BigDecimal weight; // in kg

    @NotBlank(message = "Shipping method is required")
    private String shippingMethod;
}
