package tn.fst.backend.backend.dto;

import lombok.Data;
import tn.fst.backend.backend.entity.OrderStatus;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class OrderSearchCriteria {
    private Long clientId;
    private OrderStatus status;
    private LocalDate startDate;
    private LocalDate endDate;
    private BigDecimal minAmount;
    private BigDecimal maxAmount;
}