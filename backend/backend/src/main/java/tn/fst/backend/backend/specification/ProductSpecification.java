package tn.fst.backend.backend.specification;

import org.springframework.data.jpa.domain.Specification;
import tn.fst.backend.backend.dto.ProductSearchRequest;
import tn.fst.backend.backend.entity.Product;
import tn.fst.backend.backend.entity.Category;
import tn.fst.backend.backend.entity.ProductVariant;

import jakarta.persistence.criteria.*;
import java.util.ArrayList;
import java.util.List;

public class ProductSpecification {

    public static Specification<Product> buildSpecification(ProductSearchRequest request) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (request.getKeyword() != null && !request.getKeyword().trim().isEmpty()) {
                String keyword = "%" + request.getKeyword().toLowerCase() + "%";
                Predicate namePredicate = criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("name")), keyword
                );
                Predicate descPredicate = criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("description")), keyword
                );
                predicates.add(criteriaBuilder.or(namePredicate, descPredicate));
            }

            //  Filter by category
            if (request.getCategoryId() != null) {
                Join<Product, Category> categoryJoin = root.join("category", JoinType.LEFT);
                predicates.add(criteriaBuilder.equal(
                        categoryJoin.get("idCategory"), request.getCategoryId()));
            }

            //  Filter by minimum price
            if (request.getMinPrice() != null) {
                predicates.add(
                        criteriaBuilder.greaterThanOrEqualTo(
                                root.get("price"),
                                request.getMinPrice()
                        )
                );
            }

            // Filter by maximum price
            if (request.getMaxPrice() != null) {
                predicates.add(
                        criteriaBuilder.lessThanOrEqualTo(
                                root.get("price"),
                                request.getMaxPrice()
                        )
                );
            }

            // Filter by stock availability
            if (request.getInStock() != null && request.getInStock()) {
                Subquery<Long> variantSubquery = query.subquery(Long.class);
                Root<ProductVariant> variantRoot = variantSubquery.from(ProductVariant.class);

                variantSubquery.select(criteriaBuilder.count(variantRoot.get("idVariant")))
                        .where(
                                criteriaBuilder.equal(variantRoot.get("product"), root),
                                criteriaBuilder.greaterThan(variantRoot.get("stockQuantity"), 0)
                        );

                predicates.add(criteriaBuilder.greaterThan(variantSubquery, 0L));
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }

    public static Specification<Product> hasKeyword(String keyword) {
        return (root, query, criteriaBuilder) -> {
            if (keyword == null || keyword.trim().isEmpty()) {
                return criteriaBuilder.conjunction(); // Always true
            }
            String searchTerm = "%" + keyword.toLowerCase() + "%";
            return criteriaBuilder.or(
                    criteriaBuilder.like(criteriaBuilder.lower(root.get("name")), searchTerm),
                    criteriaBuilder.like(criteriaBuilder.lower(root.get("description")), searchTerm)
            );
        };
    }

    /**
     * Filter by category
     */
    public static Specification<Product> hasCategory(Long categoryId) {
        return (root, query, criteriaBuilder) -> {
            if (categoryId == null) {
                return criteriaBuilder.conjunction();
            }
            Join<Product, Category> categoryJoin = root.join("category", JoinType.LEFT);
            return criteriaBuilder.equal(categoryJoin.get("idCategory"), categoryId);
        };
    }

    /**
     * Filter by price range
     */
    public static Specification<Product> hasPriceBetween(
            java.math.BigDecimal minPrice,
            java.math.BigDecimal maxPrice) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (minPrice != null) {
                predicates.add(
                        criteriaBuilder.greaterThanOrEqualTo(root.get("price"), minPrice)
                );
            }
            if (maxPrice != null) {
                predicates.add(
                        criteriaBuilder.lessThanOrEqualTo(root.get("price"), maxPrice)
                );
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }

    /**
     * Filter by stock availability
     */
    public static Specification<Product> isInStock() {
        return (root, query, criteriaBuilder) -> {
            Subquery<Long> variantSubquery = query.subquery(Long.class);
            Root<ProductVariant> variantRoot = variantSubquery.from(ProductVariant.class);

            variantSubquery.select(criteriaBuilder.count(variantRoot.get("idVariant")))
                    .where(
                            criteriaBuilder.equal(variantRoot.get("product"), root),
                            criteriaBuilder.greaterThan(variantRoot.get("stockQuantity"), 0)
                    );

            return criteriaBuilder.greaterThan(variantSubquery, 0L);
        };
    }
}