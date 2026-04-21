package tn.fst.backend.backend.dto;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;


@Data
@NoArgsConstructor
@AllArgsConstructor
public class SupplierRequest {

    @NotBlank(message = "Supplier name is required")
    @Size(max = 200)
    private String name;

    @Size(max = 50)
    private String code;

    @Size(max = 500)
    private String description;

    @Size(max = 100)
    private String contactPerson;

    @Email
    @Size(max = 100)
    private String email;

    @Size(max = 20)
    private String phone;

    @Size(max = 20)
    private String mobile;

    @Size(max = 200)
    private String addressLine1;

    @Size(max = 200)
    private String addressLine2;

    @Size(max = 100)
    private String city;

    @Size(max = 100)
    private String state;

    @Size(max = 20)
    private String postalCode;

    @Size(max = 100)
    private String country;

    @Size(max = 50)
    private String taxId;

    @Size(max = 50)
    private String website;

    private Integer paymentTerms; // en jours

    private Integer leadTime; // en jours

    @DecimalMin("0.00")
    private BigDecimal minimumOrder;

    @Size(max = 1000)
    private String notes;
}


