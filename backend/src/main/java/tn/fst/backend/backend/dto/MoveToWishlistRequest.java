package tn.fst.backend.backend.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MoveToWishlistRequest {

    @NotNull(message = "Cart Item ID is required")
    private Long cartItemId;
}
