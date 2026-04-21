package tn.fst.backend.backend.repository;

import tn.fst.backend.backend.entity.Product;
import tn.fst.backend.backend.entity.ProductVariant;
import tn.fst.backend.backend.entity.Stock;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;


@Repository
public interface StockRepository extends JpaRepository<Stock, Long> {
    Optional<Stock> findByProduct(Product product);
    Optional<Stock> findByVariant(ProductVariant variant);
    Optional<Stock> findByProductAndVariantIsNull(Product product);
}

