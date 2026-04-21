package tn.fst.backend.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import tn.fst.backend.backend.entity.Cart;
import tn.fst.backend.backend.entity.CartItem;
import tn.fst.backend.backend.entity.ProductVariant;


import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface CartItemRepository extends JpaRepository<CartItem, Long> {

    // Find all items in a cart
    List<CartItem> findByCart(Cart cart);

    // Find specific item in cart by variant
    Optional<CartItem> findByCartAndVariant(Cart cart, ProductVariant variant);

    // Find items added before a certain date (for expiry)
    List<CartItem> findByAddedAtBefore(LocalDateTime date);

    // Delete all items in a cart
    void deleteByCart(Cart cart);
}