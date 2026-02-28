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
public class PaymentMethodInfo {
    private String code;
    private String name;
    private String description;
    private String icon;
    private Boolean recommended;
    private List<String> supportedCards;
}

