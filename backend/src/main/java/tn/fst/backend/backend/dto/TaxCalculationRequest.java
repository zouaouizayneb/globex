package tn.fst.backend.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TaxCalculationRequest {
    @NotNull(message = "Subtotal is required")
    private BigDecimal subtotal;

    @NotBlank(message = "Country is required")
    private String country;

    private String state;
}
