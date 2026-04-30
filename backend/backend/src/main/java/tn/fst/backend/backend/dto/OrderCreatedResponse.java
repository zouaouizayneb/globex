package tn.fst.backend.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Lightweight response returned to the frontend after placing an order via POST /api/orders.
 * Contains the real DB-generated order ID and a human-readable order number.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderCreatedResponse {

    /** The numeric database ID of the newly created order. */
    private Long id;

    /** Human-readable order number, e.g. "ORD-000042". */
    private String orderNumber;

    /** Current status — always "PENDING" for a freshly created order. */
    private String status;

    /** Optional confirmation message. */
    private String message;
}
