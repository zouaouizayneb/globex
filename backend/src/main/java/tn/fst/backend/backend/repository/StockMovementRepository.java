package tn.fst.backend.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import tn.fst.backend.backend.entity.MovementType;
import tn.fst.backend.backend.entity.ProductVariant;
import tn.fst.backend.backend.entity.StockMovement;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface StockMovementRepository extends JpaRepository<StockMovement, Long> {

    List<StockMovement> findByVariantOrderByCreatedAtDesc(ProductVariant variant);

    List<StockMovement> findByVariantAndCreatedAtBetween(
            ProductVariant variant,
            LocalDateTime start,
            LocalDateTime end
    );

    List<StockMovement> findByVariantAndType(ProductVariant variant, MovementType type);

    Long countByVariant(ProductVariant variant);
}

