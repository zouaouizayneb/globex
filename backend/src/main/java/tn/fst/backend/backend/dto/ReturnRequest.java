package tn.fst.backend.backend.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReturnRequest {
    @NotBlank(message = "Return reason is required")
    private String reason;

    private String description;

    private List<Long> itemIds; // IDs des items à retourner (optionnel)
}
