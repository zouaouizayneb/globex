package tn.fst.backend.backend.dto;

import java.math.BigDecimal;

public class WishlistItemResponse {

    private Long wishlistItemId;
    private Long variantId;
    private String sku;
    private String productName;
    private String variantDetails;
    private String imageUrl;
    private BigDecimal currentPrice;
    private Boolean inStock;
    private Integer availableStock;
    private String addedAt;

    public WishlistItemResponse() {}

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private Long wishlistItemId;
        private Long variantId;
        private String sku;
        private String productName;
        private String variantDetails;
        private String imageUrl;
        private BigDecimal currentPrice;
        private Boolean inStock;
        private Integer availableStock;
        private String addedAt;

        public Builder wishlistItemId(Long wishlistItemId) {
            this.wishlistItemId = wishlistItemId;
            return this;
        }

        public Builder variantId(Long variantId) {
            this.variantId = variantId;
            return this;
        }

        public Builder sku(String sku) {
            this.sku = sku;
            return this;
        }

        public Builder productName(String productName) {
            this.productName = productName;
            return this;
        }

        public Builder variantDetails(String variantDetails) {
            this.variantDetails = variantDetails;
            return this;
        }

        public Builder imageUrl(String imageUrl) {
            this.imageUrl = imageUrl;
            return this;
        }

        public Builder currentPrice(BigDecimal currentPrice) {
            this.currentPrice = currentPrice;
            return this;
        }

        public Builder inStock(Boolean inStock) {
            this.inStock = inStock;
            return this;
        }

        public Builder availableStock(Integer availableStock) {
            this.availableStock = availableStock;
            return this;
        }

        public Builder addedAt(String addedAt) {
            this.addedAt = addedAt;
            return this;
        }

        public WishlistItemResponse build() {
            WishlistItemResponse r = new WishlistItemResponse();
            r.wishlistItemId = wishlistItemId;
            r.variantId = variantId;
            r.sku = sku;
            r.productName = productName;
            r.variantDetails = variantDetails;
            r.imageUrl = imageUrl;
            r.currentPrice = currentPrice;
            r.inStock = inStock;
            r.availableStock = availableStock;
            r.addedAt = addedAt;
            return r;
        }
    }

    public Long getWishlistItemId() {
        return wishlistItemId;
    }

    public void setWishlistItemId(Long wishlistItemId) {
        this.wishlistItemId = wishlistItemId;
    }

    public Long getVariantId() {
        return variantId;
    }

    public void setVariantId(Long variantId) {
        this.variantId = variantId;
    }

    public String getSku() {
        return sku;
    }

    public void setSku(String sku) {
        this.sku = sku;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public String getVariantDetails() {
        return variantDetails;
    }

    public void setVariantDetails(String variantDetails) {
        this.variantDetails = variantDetails;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public BigDecimal getCurrentPrice() {
        return currentPrice;
    }

    public void setCurrentPrice(BigDecimal currentPrice) {
        this.currentPrice = currentPrice;
    }

    public Boolean getInStock() {
        return inStock;
    }

    public void setInStock(Boolean inStock) {
        this.inStock = inStock;
    }

    public Integer getAvailableStock() {
        return availableStock;
    }

    public void setAvailableStock(Integer availableStock) {
        this.availableStock = availableStock;
    }

    public String getAddedAt() {
        return addedAt;
    }

    public void setAddedAt(String addedAt) {
        this.addedAt = addedAt;
    }
}
