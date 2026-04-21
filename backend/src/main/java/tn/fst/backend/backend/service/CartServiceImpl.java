package tn.fst.backend.backend.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tn.fst.backend.backend.dto.*;
import tn.fst.backend.backend.entity.*;
import tn.fst.backend.backend.exeptions.InsufficientStockException;
import tn.fst.backend.backend.exeptions.ResourceNotFoundException;
import tn.fst.backend.backend.repository.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class CartServiceImpl implements CartService {

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final WishlistRepository wishlistRepository;
    private final WishlistItemRepository wishlistItemRepository;
    private final UserRepository userRepository;
    private final ProductVariantRepository variantRepository;

    private static final int CART_EXPIRY_DAYS = 30; // Cart items expire after 30 days
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Override
    @Transactional(readOnly = true)
    public CartResponse getCartForUser(Long userId) {
        User user = getUserById(userId);
        Cart cart = getOrCreateCart(user);
        return mapToCartResponse(cart);
    }

    @Override
    public CartResponse addToCart(Long userId, AddToCartRequest request) {
        User user = getUserById(userId);
        Cart cart = getOrCreateCart(user);
        ProductVariant variant = getVariantById(request.getVariantId());

        // Check if variant has sufficient stock
        if (variant.getStockQuantity() < request.getQuantity()) {
            throw new InsufficientStockException(
                    variant.getProduct().getName(),
                    request.getQuantity(),
                    variant.getStockQuantity()
            );
        }

        // Check if item already exists in cart
        CartItem existingItem = cartItemRepository
                .findByCartAndVariant(cart, variant)
                .orElse(null);

        if (existingItem != null) {
            // Update quantity
            int newQuantity = existingItem.getQuantity() + request.getQuantity();

            // Check stock for new quantity
            if (variant.getStockQuantity() < newQuantity) {
                throw new InsufficientStockException(
                        variant.getProduct().getName(),
                        newQuantity,
                        variant.getStockQuantity()
                );
            }

            existingItem.setQuantity(newQuantity);
            cartItemRepository.save(existingItem);
        } else {
            // Create new cart item
            CartItem newItem = CartItem.builder()
                    .cart(cart)
                    .variant(variant)
                    .quantity(request.getQuantity())
                    .priceAtAdd(variant.getTotalPrice())
                    .build();

            cart.addItem(newItem);
            cartItemRepository.save(newItem);
        }

        cart.setUpdatedAt(LocalDateTime.now());
        cartRepository.save(cart);

        return mapToCartResponse(cart);
    }

    @Override
    public CartResponse updateCartItemQuantity(Long userId, Long cartItemId, UpdateCartItemRequest request) {
        User user = getUserById(userId);
        Cart cart = getOrCreateCart(user);

        CartItem cartItem = cartItemRepository.findById(cartItemId)
                .orElseThrow(() -> new ResourceNotFoundException("Cart item", cartItemId));

        // Verify cart item belongs to user's cart
        if (!cartItem.getCart().getIdCart().equals(cart.getIdCart())) {
            throw new IllegalArgumentException("Cart item does not belong to this user");
        }

        // Check stock availability
        if (cartItem.getVariant().getStockQuantity() < request.getQuantity()) {
            throw new InsufficientStockException(
                    cartItem.getProductName(),
                    request.getQuantity(),
                    cartItem.getVariant().getStockQuantity()
            );
        }

        cartItem.setQuantity(request.getQuantity());
        cartItemRepository.save(cartItem);

        cart.setUpdatedAt(LocalDateTime.now());
        cartRepository.save(cart);

        return mapToCartResponse(cart);
    }

    @Override
    public CartResponse removeFromCart(Long userId, Long cartItemId) {
        User user = getUserById(userId);
        Cart cart = getOrCreateCart(user);

        CartItem cartItem = cartItemRepository.findById(cartItemId)
                .orElseThrow(() -> new ResourceNotFoundException("Cart item", cartItemId));

        // Verify ownership
        if (!cartItem.getCart().getIdCart().equals(cart.getIdCart())) {
            throw new IllegalArgumentException("Cart item does not belong to this user");
        }

        cart.removeItem(cartItem);
        cartItemRepository.delete(cartItem);

        cart.setUpdatedAt(LocalDateTime.now());
        cartRepository.save(cart);

        return mapToCartResponse(cart);
    }

    @Override
    public void clearCart(Long userId) {
        User user = getUserById(userId);
        Cart cart = cartRepository.findByUser(user).orElse(null);

        if (cart != null) {
            cart.clearItems();
            cartItemRepository.deleteByCart(cart);

            cart.setUpdatedAt(LocalDateTime.now());
            cartRepository.save(cart);
        }
    }

    @Override
    public CartResponse moveToWishlist(Long userId, Long cartItemId) {
        User user = getUserById(userId);
        Cart cart = getOrCreateCart(user);
        Wishlist wishlist = getOrCreateWishlist(user);

        CartItem cartItem = cartItemRepository.findById(cartItemId)
                .orElseThrow(() -> new ResourceNotFoundException("Cart item", cartItemId));

        // Verify ownership
        if (!cartItem.getCart().getIdCart().equals(cart.getIdCart())) {
            throw new IllegalArgumentException("Cart item does not belong to this user");
        }

        // Check if item already in wishlist
        boolean alreadyInWishlist = wishlistItemRepository
                .existsByWishlistAndVariant(wishlist, cartItem.getVariant());

        if (!alreadyInWishlist) {
            // Add to wishlist
            WishlistItem wishlistItem = WishlistItem.builder()
                    .wishlist(wishlist)
                    .variant(cartItem.getVariant())
                    .build();

            wishlist.addItem(wishlistItem);
            wishlistItemRepository.save(wishlistItem);
            wishlistRepository.save(wishlist);
        }

        // Remove from cart
        cart.removeItem(cartItem);
        cartItemRepository.delete(cartItem);

        cart.setUpdatedAt(LocalDateTime.now());
        cartRepository.save(cart);

        return mapToCartResponse(cart);
    }

    @Override
    public CartResponse moveToCart(Long userId, Long wishlistItemId) {
        User user = getUserById(userId);
        Cart cart = getOrCreateCart(user);
        Wishlist wishlist = getOrCreateWishlist(user);

        WishlistItem wishlistItem = wishlistItemRepository.findById(wishlistItemId)
                .orElseThrow(() -> new ResourceNotFoundException("Wishlist item", wishlistItemId));

        // Verify ownership
        if (!wishlistItem.getWishlist().getIdWishlist().equals(wishlist.getIdWishlist())) {
            throw new IllegalArgumentException("Wishlist item does not belong to this user");
        }

        ProductVariant variant = wishlistItem.getVariant();

        // Check stock
        if (!variant.isInStock()) {
            throw new IllegalStateException("Product is out of stock");
        }

        // Check if already in cart
        CartItem existingCartItem = cartItemRepository
                .findByCartAndVariant(cart, variant)
                .orElse(null);

        if (existingCartItem != null) {
            // Just increase quantity
            existingCartItem.setQuantity(existingCartItem.getQuantity() + 1);
            cartItemRepository.save(existingCartItem);
        } else {
            // Add to cart
            CartItem cartItem = CartItem.builder()
                    .cart(cart)
                    .variant(variant)
                    .quantity(1)
                    .priceAtAdd(variant.getTotalPrice())
                    .build();

            cart.addItem(cartItem);
            cartItemRepository.save(cartItem);
        }

        // Remove from wishlist
        wishlist.removeItem(wishlistItem);
        wishlistItemRepository.delete(wishlistItem);

        cart.setUpdatedAt(LocalDateTime.now());
        cartRepository.save(cart);
        wishlistRepository.save(wishlist);

        return mapToCartResponse(cart);
    }

    @Override
    public void removeExpiredCartItems() {
        LocalDateTime expiryDate = LocalDateTime.now().minusDays(CART_EXPIRY_DAYS);
        List<CartItem> expiredItems = cartItemRepository.findByAddedAtBefore(expiryDate);
        cartItemRepository.deleteAll(expiredItems);
    }

    // ==================== HELPER METHODS ====================

    private User getUserById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", userId));
    }

    private ProductVariant getVariantById(Long variantId) {
        return variantRepository.findById(variantId)
                .orElseThrow(() -> new ResourceNotFoundException("Product variant", variantId));
    }

    private Cart getOrCreateCart(User user) {
        return cartRepository.findByUser(user)
                .orElseGet(() -> {
                    Cart newCart = Cart.builder()
                            .user(user)
                            .build();
                    return cartRepository.save(newCart);
                });
    }

    private Wishlist getOrCreateWishlist(User user) {
        return wishlistRepository.findByUser(user)
                .orElseGet(() -> {
                    Wishlist newWishlist = Wishlist.builder()
                            .user(user)
                            .build();
                    return wishlistRepository.save(newWishlist);
                });
    }

    private CartResponse mapToCartResponse(Cart cart) {
        List<CartItemResponse> itemResponses = cart.getItems().stream()
                .map(this::mapToCartItemResponse)
                .collect(Collectors.toList());

        return CartResponse.builder()
                .cartId(cart.getIdCart())
                .userId(cart.getUser().getIdUser())
                .items(itemResponses)
                .totalItems(cart.getTotalItems())
                .totalPrice(cart.getTotalPrice())
                .createdAt(cart.getCreatedAt().format(FORMATTER))
                .updatedAt(cart.getUpdatedAt().format(FORMATTER))
                .build();
    }

    private CartItemResponse mapToCartItemResponse(CartItem item) {
        ProductVariant variant = item.getVariant();
        Product product = variant.getProduct();

        return CartItemResponse.builder()
                .cartItemId(item.getIdCartItem())
                .variantId(variant.getIdVariant())
                .sku(variant.getSku())
                .productName(product.getName())
                .variantDetails(item.getVariantDetails())
                .imageUrl(product.getPrimaryImageUrl())
                .quantity(item.getQuantity())
                .pricePerUnit(item.getPriceAtAdd())
                .subtotal(item.getSubtotal())
                .inStock(variant.isInStock())
                .availableStock(variant.getStockQuantity())
                .addedAt(item.getAddedAt().format(FORMATTER))
                .build();
    }
}