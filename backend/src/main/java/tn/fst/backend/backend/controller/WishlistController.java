package tn.fst.backend.backend.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import tn.fst.backend.backend.dto.WishlistResponse;
import tn.fst.backend.backend.entity.User;
import tn.fst.backend.backend.service.CartService;
import tn.fst.backend.backend.service.WishlistService;

@RestController
@RequestMapping("/api/wishlist")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class WishlistController {

    private final WishlistService wishlistService;
    private final CartService cartService;

    /**
     * Get current user's wishlist
     * GET /api/wishlist
     */
    @GetMapping
    public ResponseEntity<WishlistResponse> getWishlist(Authentication authentication) {
        Long userId = getCurrentUserId(authentication);
        WishlistResponse wishlist = wishlistService.getWishlistForUser(userId);
        return ResponseEntity.ok(wishlist);
    }

    /**
     * Add item to wishlist
     * POST /api/wishlist/items/{variantId}
     */
    @PostMapping("/items/{variantId}")
    public ResponseEntity<WishlistResponse> addToWishlist(
            @PathVariable Long variantId,
            Authentication authentication) {

        Long userId = getCurrentUserId(authentication);
        WishlistResponse wishlist = wishlistService.addToWishlist(userId, variantId);
        return new ResponseEntity<>(wishlist, HttpStatus.CREATED);
    }

    /**
     * Remove item from wishlist
     * DELETE /api/wishlist/items/{wishlistItemId}
     */
    @DeleteMapping("/items/{wishlistItemId}")
    public ResponseEntity<WishlistResponse> removeFromWishlist(
            @PathVariable Long wishlistItemId,
            Authentication authentication) {

        Long userId = getCurrentUserId(authentication);
        WishlistResponse wishlist = wishlistService.removeFromWishlist(userId, wishlistItemId);
        return ResponseEntity.ok(wishlist);
    }

    /**
     * Clear entire wishlist
     * DELETE /api/wishlist
     */
    @DeleteMapping
    public ResponseEntity<Void> clearWishlist(Authentication authentication) {
        Long userId = getCurrentUserId(authentication);
        wishlistService.clearWishlist(userId);
        return ResponseEntity.noContent().build();
    }

    /**
     * Move item from wishlist to cart
     * POST /api/wishlist/items/{wishlistItemId}/move-to-cart
     */
    @PostMapping("/items/{wishlistItemId}/move-to-cart")
    public ResponseEntity<Void> moveToCart(
            @PathVariable Long wishlistItemId,
            Authentication authentication) {

        Long userId = getCurrentUserId(authentication);
        cartService.moveToCart(userId, wishlistItemId);
        return ResponseEntity.ok().build();
    }

    /**
     * Get wishlist count (for badge)
     * GET /api/wishlist/count
     */
    @GetMapping("/count")
    public ResponseEntity<WishlistCountResponse> getWishlistCount(Authentication authentication) {
        Long userId = getCurrentUserId(authentication);
        WishlistResponse wishlist = wishlistService.getWishlistForUser(userId);

        WishlistCountResponse count = new WishlistCountResponse(wishlist.getTotalItems());
        return ResponseEntity.ok(count);
    }

    private Long getCurrentUserId(Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        return user.getIdUser();
    }

    public record WishlistCountResponse(Integer count) {}
}