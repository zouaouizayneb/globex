package tn.fst.backend.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import tn.fst.backend.backend.entity.Product;
import tn.fst.backend.backend.entity.ProductVariant;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductVariantRepository extends JpaRepository<ProductVariant, Long> {

    List<ProductVariant> findByProduct(Product product);

    Optional<ProductVariant> findBySku(String sku);

    List<ProductVariant> findByStockQuantityGreaterThan(Integer quantity);

    List<ProductVariant> findByStockQuantityGreaterThanEqual(Integer quantity);

    boolean existsBySku(String sku);

    List<ProductVariant> findByStockQuantityLessThanEqual(Integer threshold);

    List<ProductVariant> findByStockQuantity(Integer quantity);

}