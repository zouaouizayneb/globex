package tn.fst.backend.backend.service;

import tn.fst.backend.backend.dto.*;

public interface CartService {

    // Cart operations
    CartResponse getCartForUser(Long userId);
    CartResponse addToCart(Long userId, AddToCartRequest request);
    CartResponse updateCartItemQuantity(Long userId, Long cartItemId, UpdateCartItemRequest request);
    CartResponse removeFromCart(Long userId, Long cartItemId);
    void clearCart(Long userId);

    // Move between cart and wishlist
    CartResponse moveToWishlist(Long userId, Long cartItemId);
    CartResponse moveToCart(Long userId, Long wishlistItemId);

    // Utility
    void removeExpiredCartItems();
}