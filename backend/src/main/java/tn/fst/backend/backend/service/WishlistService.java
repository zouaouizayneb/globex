package tn.fst.backend.backend.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tn.fst.backend.backend.dto.WishlistItemResponse;
import tn.fst.backend.backend.dto.WishlistResponse;
import tn.fst.backend.backend.entity.*;
import tn.fst.backend.backend.exeptions.ResourceNotFoundException;
import tn.fst.backend.backend.repository.*;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class WishlistService {

    private final WishlistRepository wishlistRepository;
    private final WishlistItemRepository wishlistItemRepository;
    private final UserRepository userRepository;
    private final ProductVariantRepository variantRepository;

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Transactional(readOnly = true)
    public WishlistResponse getWishlistForUser(Long userId) {
        User user = getUserById(userId);
        Wishlist wishlist = getOrCreateWishlist(user);
        return mapToWishlistResponse(wishlist);
    }

    public WishlistResponse addToWishlist(Long userId, Long variantId) {
        User user = getUserById(userId);
        Wishlist wishlist = getOrCreateWishlist(user);
        ProductVariant variant = getVariantById(variantId);

        // Check if already in wishlist
        boolean exists = wishlistItemRepository
                .existsByWishlistAndVariant(wishlist, variant);

        if (exists) {
            throw new IllegalArgumentException("Item already in wishlist");
        }

        // Add to wishlist
        WishlistItem item = WishlistItem.builder()
                .wishlist(wishlist)
                .variant(variant)
                .build();

        wishlist.addItem(item);
        wishlistItemRepository.save(item);
        wishlistRepository.save(wishlist);

        return mapToWishlistResponse(wishlist);
    }

    public WishlistResponse removeFromWishlist(Long userId, Long wishlistItemId) {
        User user = getUserById(userId);
        Wishlist wishlist = getOrCreateWishlist(user);

        WishlistItem item = wishlistItemRepository.findById(wishlistItemId)
                .orElseThrow(() -> new ResourceNotFoundException("Wishlist item", wishlistItemId));

        // Verify ownership
        if (!item.getWishlist().getIdWishlist().equals(wishlist.getIdWishlist())) {
            throw new IllegalArgumentException("Wishlist item does not belong to this user");
        }

        wishlist.removeItem(item);
        wishlistItemRepository.delete(item);
        wishlistRepository.save(wishlist);

        return mapToWishlistResponse(wishlist);
    }

    public void clearWishlist(Long userId) {
        User user = getUserById(userId);
        Wishlist wishlist = wishlistRepository.findByUser(user).orElse(null);

        if (wishlist != null) {
            wishlist.clearItems();
            wishlistItemRepository.deleteByWishlist(wishlist);
            wishlistRepository.save(wishlist);
        }
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

    private Wishlist getOrCreateWishlist(User user) {
        return wishlistRepository.findByUser(user)
                .orElseGet(() -> {
                    Wishlist newWishlist = Wishlist.builder()
                            .user(user)
                            .build();
                    return wishlistRepository.save(newWishlist);
                });
    }

    private WishlistResponse mapToWishlistResponse(Wishlist wishlist) {
        List<WishlistItemResponse> itemResponses = wishlist.getItems().stream()
                .map(this::mapToWishlistItemResponse)
                .collect(Collectors.toList());

        return WishlistResponse.builder()
                .wishlistId(wishlist.getIdWishlist())
                .userId(wishlist.getUser().getIdUser())
                .items(itemResponses)
                .totalItems(wishlist.getTotalItems())
                .createdAt(wishlist.getCreatedAt().format(FORMATTER))
                .updatedAt(wishlist.getUpdatedAt().format(FORMATTER))
                .build();
    }

    private WishlistItemResponse mapToWishlistItemResponse(WishlistItem item) {
        ProductVariant variant = item.getVariant();
        Product product = variant.getProduct();

        return WishlistItemResponse.builder()
                .wishlistItemId(item.getIdWishlistItem())
                .variantId(variant.getIdVariant())
                .sku(variant.getSku())
                .productName(product.getName())
                .variantDetails(getVariantDetails(variant))
                .imageUrl(product.getPrimaryImageUrl())
                .currentPrice(variant.getTotalPrice())
                .inStock(variant.isInStock())
                .availableStock(variant.getStockQuantity())
                .addedAt(item.getAddedAt().format(FORMATTER))
                .build();
    }

    private String getVariantDetails(ProductVariant variant) {
        StringBuilder details = new StringBuilder();
        if (variant.getSize() != null) {
            details.append(variant.getSize());
        }
        if (variant.getColor() != null) {
            if (details.length() > 0) details.append(", ");
            details.append(variant.getColor());
        }
        return details.toString();
    }
}