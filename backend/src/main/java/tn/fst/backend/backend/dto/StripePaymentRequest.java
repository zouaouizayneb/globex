package tn.fst.backend.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class StripePaymentRequest {
    @NotNull(message = "Amount is required")
    private Long amount; // in cents

    @NotBlank(message = "Currency is required")
    private String currency;

    private String description;
}
