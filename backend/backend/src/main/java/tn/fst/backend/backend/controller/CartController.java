package tn.fst.backend.backend.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import tn.fst.backend.backend.dto.*;
import tn.fst.backend.backend.entity.User;
import tn.fst.backend.backend.service.CartService;
import tn.fst.backend.backend.service.WishlistService;

@RestController
@RequestMapping("/api/cart")
@CrossOrigin(origins = "http://localhost:4200")
@RequiredArgsConstructor
public class CartController {

    private final CartService cartService;
    private final WishlistService wishlistService;

    /**
     * Get current user's cart
     * GET /api/cart
     */
    @GetMapping
    public ResponseEntity<CartResponse> getCart(Authentication authentication) {
        Long userId = getCurrentUserId(authentication);
        CartResponse cart = cartService.getCartForUser(userId);
        return ResponseEntity.ok(cart);
    }

    /**
     * Add item to cart
     * POST /api/cart/items
     * Body: { "variantId": 1, "quantity": 2 }
     */
    @PostMapping("/items")
    public ResponseEntity<CartResponse> addToCart(
            @Valid @RequestBody AddToCartRequest request,
            Authentication authentication) {

        Long userId = getCurrentUserId(authentication);
        CartResponse cart = cartService.addToCart(userId, request);
        return new ResponseEntity<>(cart, HttpStatus.CREATED);
    }

    /**
     * Update cart item quantity
     * PUT /api/cart/items/{cartItemId}
     * Body: { "quantity": 3 }
     */
    @PutMapping("/items/{cartItemId}")
    public ResponseEntity<CartResponse> updateCartItem(
            @PathVariable Long cartItemId,
            @Valid @RequestBody UpdateCartItemRequest request,
            Authentication authentication) {

        Long userId = getCurrentUserId(authentication);
        CartResponse cart = cartService.updateCartItemQuantity(userId, cartItemId, request);
        return ResponseEntity.ok(cart);
    }

    /**
     * Remove item from cart
     * DELETE /api/cart/items/{cartItemId}
     */
    @DeleteMapping("/items/{cartItemId}")
    public ResponseEntity<CartResponse> removeFromCart(
            @PathVariable Long cartItemId,
            Authentication authentication) {

        Long userId = getCurrentUserId(authentication);
        CartResponse cart = cartService.removeFromCart(userId, cartItemId);
        return ResponseEntity.ok(cart);
    }

    /**
     * Clear entire cart
     * DELETE /api/cart
     */
    @DeleteMapping
    public ResponseEntity<Void> clearCart(Authentication authentication) {
        Long userId = getCurrentUserId(authentication);
        cartService.clearCart(userId);
        return ResponseEntity.noContent().build();
    }

    /**
     * Move item from cart to wishlist
     * POST /api/cart/items/{cartItemId}/move-to-wishlist
     */
    @PostMapping("/items/{cartItemId}/move-to-wishlist")
    public ResponseEntity<CartResponse> moveToWishlist(
            @PathVariable Long cartItemId,
            Authentication authentication) {

        Long userId = getCurrentUserId(authentication);
        CartResponse cart = cartService.moveToWishlist(userId, cartItemId);
        return ResponseEntity.ok(cart);
    }

    /**
     * Get cart summary (for header/badge)
     * GET /api/cart/summary
     */
    @GetMapping("/summary")
    public ResponseEntity<CartSummaryResponse> getCartSummary(Authentication authentication) {
        Long userId = getCurrentUserId(authentication);
        CartResponse cart = cartService.getCartForUser(userId);

        CartSummaryResponse summary = new CartSummaryResponse(
                cart.getTotalItems(),
                cart.getTotalPrice()
        );

        return ResponseEntity.ok(summary);
    }

    private Long getCurrentUserId(Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        return user.getIdUser();
    }

    public record CartSummaryResponse(Integer totalItems, java.math.BigDecimal totalPrice) {}
}