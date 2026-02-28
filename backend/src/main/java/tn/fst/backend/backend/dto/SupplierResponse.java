package tn.fst.backend.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SupplierResponse {
    private Long supplierId;
    private String name;
    private String code;
    private String description;
    private String contactPerson;
    private String email;
    private String phone;
    private String mobile;
    private String addressLine1;
    private String addressLine2;
    private String city;
    private String state;
    private String postalCode;
    private String country;
    private String formattedAddress;
    private String taxId;
    private String website;
    private Integer paymentTerms;
    private Integer leadTime;
    private BigDecimal minimumOrder;
    private String status;
    private BigDecimal rating;
    private BigDecimal qualityRating;
    private BigDecimal deliveryRating;
    private BigDecimal serviceRating;
    private Integer totalOrders;
    private BigDecimal totalSpent;
    private Double onTimeDeliveryRate;
    private LocalDate firstOrderDate;
    private LocalDate lastOrderDate;
    private String notes;
    private LocalDateTime createdAt;
}
