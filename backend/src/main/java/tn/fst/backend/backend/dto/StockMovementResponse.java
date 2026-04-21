package tn.fst.backend.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StockMovementResponse {
    private Long movementId;
    private String type; // SALE, PURCHASE, ADJUSTMENT, etc.
    private Integer quantity;
    private Integer stockBefore;
    private Integer stockAfter;
    private String referenceType;
    private Long referenceId;
    private String reason;
    private LocalDateTime createdAt;
}
