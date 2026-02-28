package tn.fst.backend.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ShippingDetails {
    private Long shipmentId;
    private String carrier;
    private String method;
    private String trackingNumber;
    private LocalDate estimatedDelivery;
    private String status;
    private AddressResponse shippingAddress;
}
