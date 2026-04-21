package tn.fst.backend.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import tn.fst.backend.backend.entity.Wishlist;
import tn.fst.backend.backend.entity.WishlistItem;
import tn.fst.backend.backend.entity.ProductVariant;


import java.util.List;
import java.util.Optional;

@Repository
public interface WishlistItemRepository extends JpaRepository<WishlistItem, Long> {

    // Find all items in a wishlist
    List<WishlistItem> findByWishlist(Wishlist wishlist);

    // Find specific item in wishlist by variant
    Optional<WishlistItem> findByWishlistAndVariant(Wishlist wishlist, ProductVariant variant);

    // Check if variant exists in wishlist
    boolean existsByWishlistAndVariant(Wishlist wishlist, ProductVariant variant);

    // Delete all items in a wishlist
    void deleteByWishlist(Wishlist wishlist);
}
