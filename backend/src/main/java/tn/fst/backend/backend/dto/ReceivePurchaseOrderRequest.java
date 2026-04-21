package tn.fst.backend.backend.dto;

import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReceivePurchaseOrderRequest {

    @NotEmpty
    private List<ReceivedItemRequest> receivedItems;

    private LocalDate receivedDate;
}
