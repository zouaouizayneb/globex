package tn.fst.backend.backend.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.fasterxml.jackson.annotation.JsonIgnore;

import java.math.BigDecimal;

@Entity
@Table(name = "product_variants")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductVariant {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_variant")
    private Long idVariant;

    @Column(unique = true, nullable = false)
    private String sku;

    private String size;

    private String color;

    @Column(precision = 10, scale = 2)
    private BigDecimal additionalPrice = BigDecimal.ZERO;
    @Column(nullable = false)
    private Integer stockQuantity = 0;

    @ManyToOne
    @JoinColumn(name = "product_id", nullable = false)
    @JsonIgnore
    private Product product;

    public BigDecimal getTotalPrice() {
        if (product != null && product.getPrice() != null) {
            return product.getPrice().add(additionalPrice);
        }
        return additionalPrice;
    }

    public boolean isInStock() {
        return stockQuantity != null && stockQuantity > 0;
    }

    public boolean canFulfill(int quantity) {
        return stockQuantity != null && stockQuantity >= quantity;
    }
}