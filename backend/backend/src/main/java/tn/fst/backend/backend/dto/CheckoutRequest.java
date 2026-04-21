package tn.fst.backend.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CheckoutRequest {

    @NotNull(message = "Shipping address ID is required")
    private Long shippingAddressId;

    private Long billingAddressId; // Optional, defaults to shipping

    @NotBlank(message = "Payment method is required")
    private String paymentMethod; // STRIPE, PAYPAL, COD

    @NotBlank(message = "Shipping method is required")
    private String shippingMethod; // STANDARD, EXPRESS, OVERNIGHT

    private String promoCode; // Optional discount code

    // For Stripe
    private String paymentIntentId;

    // Additional notes
    private String orderNotes;
}
