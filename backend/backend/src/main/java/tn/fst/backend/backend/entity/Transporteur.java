package tn.fst.backend.backend.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import java.math.BigDecimal;

@Entity
@Table(name = "transporteurs")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Transporteur {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idTransporteur;

    @Column(nullable = false)
    private String name;

    private String email;

    private String phone;

    private String address;

    @Column(nullable = false)
    private String status; // 'active' | 'inactive'

    private BigDecimal deliveryFee;

    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        if (status == null) status = "active";
    }
}
