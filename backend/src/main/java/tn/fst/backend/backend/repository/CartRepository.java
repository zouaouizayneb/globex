package tn.fst.backend.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import tn.fst.backend.backend.entity.Cart;
import tn.fst.backend.backend.entity.CartItem;
import tn.fst.backend.backend.entity.User;
import tn.fst.backend.backend.entity.ProductVariant;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface CartRepository extends JpaRepository<Cart, Long> {

    // Find cart by user
    Optional<Cart> findByUser(User user);

    // Find cart by user ID
    Optional<Cart> findByUser_IdUser(Long userId);

    // Find abandoned carts (not updated for X days)
    List<Cart> findByUpdatedAtBefore(LocalDateTime date);
}

