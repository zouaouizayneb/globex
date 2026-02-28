package tn.fst.backend.backend.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "wishlist_items")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WishlistItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_wishlist_item")
    private Long idWishlistItem;

    @ManyToOne
    @JoinColumn(name = "wishlist_id", nullable = false)
    @JsonIgnore
    private Wishlist wishlist;

    @ManyToOne
    @JoinColumn(name = "variant_id", nullable = false)
    private ProductVariant variant;

    private LocalDateTime addedAt;

    @PrePersist
    protected void onCreate() {
        addedAt = LocalDateTime.now();
    }

    public String getProductName() {
        return variant != null && variant.getProduct() != null
                ? variant.getProduct().getName()
                : "";
    }

    public java.math.BigDecimal getCurrentPrice() {
        return variant != null ? variant.getTotalPrice() : java.math.BigDecimal.ZERO;
    }

    public boolean isInStock() {
        return variant != null && variant.isInStock();
    }
}