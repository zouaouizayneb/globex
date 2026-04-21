package tn.fst.backend.backend.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PurchaseOrderRequest {

    @NotNull(message = "Supplier ID is required")
    private Long supplierId;

    private LocalDate orderDate;

    private LocalDate expectedDeliveryDate;

    @NotEmpty(message = "At least one item is required")
    private List<PurchaseOrderItemRequest> items;

    @DecimalMin("0.00")
    private BigDecimal taxAmount;

    @DecimalMin("0.00")
    private BigDecimal shippingCost;

    @DecimalMin("0.00")
    private BigDecimal discountAmount;

    @Size(max = 50)
    private String shippingMethod;

    @Size(max = 200)
    private String warehouseLocation;

    @Size(max = 1000)
    private String notes;

    @Size(max = 10)
    private String currency;
}
