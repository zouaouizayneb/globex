package tn.fst.backend.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import tn.fst.backend.backend.entity.User;
import tn.fst.backend.backend.entity.Wishlist;

import java.util.Optional;

@Repository
public interface WishlistRepository extends JpaRepository<Wishlist, Long> {

    // Find wishlist by user
    Optional<Wishlist> findByUser(User user);

    // Find wishlist by user ID
    Optional<Wishlist> findByUser_IdUser(Long userId);
}