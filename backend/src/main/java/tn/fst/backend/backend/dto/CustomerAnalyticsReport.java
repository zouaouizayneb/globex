package tn.fst.backend.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CustomerAnalyticsReport {
    private LocalDate startDate;
    private LocalDate endDate;
    private Long totalCustomers;
    private Long activeCustomers;
    private Long newCustomers;
    private BigDecimal averageCustomerValue;
    private List<TopCustomer> topCustomers;
    private String currency;
    private LocalDateTime generatedAt;
}
