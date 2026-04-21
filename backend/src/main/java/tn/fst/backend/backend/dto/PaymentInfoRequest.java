package tn.fst.backend.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PaymentInfoRequest {

    @NotBlank
    private String paymentMethod;

    @Size(max = 100)
    private String paymentReference;

    private LocalDate paymentDate;
}
