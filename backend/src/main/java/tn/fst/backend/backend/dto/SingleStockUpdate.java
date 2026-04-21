package tn.fst.backend.backend.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SingleStockUpdate {
    @NotNull
    private Long variantId;

    @NotNull
    private Integer quantityChange;

    private String reason;
}
