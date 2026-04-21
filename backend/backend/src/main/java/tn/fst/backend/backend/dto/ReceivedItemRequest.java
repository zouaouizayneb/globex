package tn.fst.backend.backend.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReceivedItemRequest {

    @NotNull
    private Long itemId;

    @NotNull
    @Min(0)
    private Integer receivedQuantity;

    @Min(0)
    private Integer damagedQuantity = 0;
}
