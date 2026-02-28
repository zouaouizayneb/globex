package tn.fst.backend.backend.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "cart_items")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CartItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_cart_item")
    private Long idCartItem;

    @ManyToOne
    @JoinColumn(name = "cart_id", nullable = false)
    @JsonIgnore
    private Cart cart;

    @ManyToOne
    @JoinColumn(name = "variant_id", nullable = false)
    private ProductVariant variant;

    @Column(nullable = false)
    private Integer quantity;

    @Column(precision = 10, scale = 2, nullable = false)
    private BigDecimal priceAtAdd;

    private LocalDateTime addedAt;

    @PrePersist
    protected void onCreate() {
        addedAt = LocalDateTime.now();
    }

    public BigDecimal getSubtotal() {
        return priceAtAdd.multiply(BigDecimal.valueOf(quantity));
    }

    public String getProductName() {
        return variant != null && variant.getProduct() != null
                ? variant.getProduct().getName()
                : "";
    }

    public String getVariantDetails() {
        if (variant == null) return "";
        StringBuilder details = new StringBuilder();
        if (variant.getSize() != null) {
            details.append(variant.getSize());
        }
        if (variant.getColor() != null) {
            if (details.length() > 0) details.append(", ");
            details.append(variant.getColor());
        }
        return details.toString();
    }
}
