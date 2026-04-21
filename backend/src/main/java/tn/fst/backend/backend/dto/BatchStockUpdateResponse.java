package tn.fst.backend.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BatchStockUpdateResponse {
    private Boolean success;
    private Integer totalUpdated;
    private Integer failed;
    private List<String> errors;
}
