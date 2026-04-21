package tn.fst.backend.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TopProductsReport {
    private LocalDate startDate;
    private LocalDate endDate;
    private List<TopProductDetail> topProducts;
    private LocalDateTime generatedAt;
}
