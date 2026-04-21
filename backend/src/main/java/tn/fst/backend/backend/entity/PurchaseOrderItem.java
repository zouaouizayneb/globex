package tn.fst.backend.backend.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Entity
@Table(name = "purchase_order_items")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PurchaseOrderItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_item")
    private Long idItem;

    @ManyToOne
    @JoinColumn(name = "purchase_order_id", nullable = false)
    private PurchaseOrder purchaseOrder;

    @ManyToOne
    @JoinColumn(name = "variant_id", nullable = false)
    private ProductVariant variant;

    @Column(nullable = false)
    private Integer quantity;

    @Column(name = "unit_cost", nullable = false, precision = 10, scale = 2)
    private BigDecimal unitCost;

    @Column(name = "line_total", nullable = false, precision = 12, scale = 2)
    private BigDecimal lineTotal;

    @Column(name = "received_quantity")
    private Integer receivedQuantity = 0;

    @Column(name = "damaged_quantity")
    private Integer damagedQuantity = 0;

    @Column(length = 500)
    private String notes;

    @PrePersist
    @PreUpdate
    protected void calculateLineTotal() {
        lineTotal = unitCost.multiply(BigDecimal.valueOf(quantity));
    }

    public boolean isFullyReceived() {
        return receivedQuantity >= quantity;
    }

    public Integer getRemainingQuantity() {
        return quantity - receivedQuantity;
    }
}
