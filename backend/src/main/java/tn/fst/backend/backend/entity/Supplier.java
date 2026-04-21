package tn.fst.backend.backend.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;


@Entity
@Table(name = "suppliers")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Supplier {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_supplier")
    private Long idSupplier;

    @Column(nullable = false, length = 200)
    private String name;

    @Column(unique = true, length = 50)
    private String code;

    @Column(length = 500)
    private String description;

    @Column(name = "contact_person", length = 100)
    private String contactPerson;

    @Column(length = 100)
    private String email;

    @Column(length = 20)
    private String phone;

    @Column(length = 20)
    private String mobile;


    @Column(name = "address_line1", length = 200)
    private String addressLine1;

    @Column(name = "address_line2", length = 200)
    private String addressLine2;

    @Column(length = 100)
    private String city;

    @Column(length = 100)
    private String state;

    @Column(name = "postal_code", length = 20)
    private String postalCode;

    @Column(length = 100)
    private String country;

    @Column(name = "tax_id", length = 50)
    private String taxId; // Numéro TVA

    @Column(length = 50)
    private String website;

    @Column(name = "payment_terms")
    private Integer paymentTerms;

    @Column(name = "lead_time")
    private Integer leadTime;

    @Column(name = "minimum_order", precision = 10, scale = 2)
    private BigDecimal minimumOrder;


    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SupplierStatus status;

    @Column(precision = 3, scale = 2)
    private BigDecimal rating;

    @Column(name = "quality_rating", precision = 3, scale = 2)
    private BigDecimal qualityRating;

    @Column(name = "delivery_rating", precision = 3, scale = 2)
    private BigDecimal deliveryRating;

    @Column(name = "service_rating", precision = 3, scale = 2)
    private BigDecimal serviceRating;

    @Column(name = "total_orders")
    private Integer totalOrders = 0;
    @Column(name = "total_spent", precision = 12, scale = 2)
    private BigDecimal totalSpent = BigDecimal.ZERO;
    @Column(name = "on_time_deliveries")
    private Integer onTimeDeliveries = 0;

    @Column(name = "late_deliveries")
    private Integer lateDeliveries = 0;

    @Column(name = "first_order_date")
    private LocalDate firstOrderDate;

    @Column(name = "last_order_date")
    private LocalDate lastOrderDate;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(length = 1000)
    private String notes;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (status == null) {
            status = SupplierStatus.ACTIVE;
        }
        if (rating == null) {
            rating = BigDecimal.valueOf(5.0);
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public String getFormattedAddress() {
        StringBuilder address = new StringBuilder();
        if (addressLine1 != null) address.append(addressLine1);
        if (addressLine2 != null && !addressLine2.isEmpty()) {
            address.append(", ").append(addressLine2);
        }
        if (city != null) address.append(", ").append(city);
        if (country != null) address.append(", ").append(country);
        return address.toString();
    }

    public double getOnTimeDeliveryRate() {
        if (totalOrders == 0) return 0;
        return (onTimeDeliveries * 100.0) / totalOrders;
    }

    public boolean isActive() {
        return status == SupplierStatus.ACTIVE;
    }
}


