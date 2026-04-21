package tn.fst.backend.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

// ==================== PAYPAL PAYMENT DTOs ====================

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PayPalPaymentRequest {

    @NotNull(message = "Amount is required")
    private BigDecimal amount;

    @NotBlank(message = "Currency is required")
    private String currency; // USD, EUR, GBP, etc.

    @NotBlank(message = "Order ID is required")
    private String orderId;

    private String description;

    @NotBlank(message = "Return URL is required")
    private String returnUrl;

    @NotBlank(message = "Cancel URL is required")
    private String cancelUrl;
}

