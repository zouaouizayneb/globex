package tn.fst.backend.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TopCustomer {
    private Long userId;
    private String customerName;
    private String email;
    private Long totalOrders;
    private BigDecimal totalSpent;
}
