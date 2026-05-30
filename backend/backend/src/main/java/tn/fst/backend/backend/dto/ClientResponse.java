package tn.fst.backend.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ClientResponse {
    private Long id;
    private String name;
    private String email;
    private String phone;
    private String country;
    private String address;
    private String status;
    private Integer totalOrders;
    private Double totalSpent;
    private String joinDate;
}
