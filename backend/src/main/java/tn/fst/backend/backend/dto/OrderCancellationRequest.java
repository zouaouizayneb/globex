package tn.fst.backend.backend.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderCancellationRequest {
    @NotBlank(message = "Cancellation reason is required")
    private String reason;
}
