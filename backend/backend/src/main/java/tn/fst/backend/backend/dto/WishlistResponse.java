package tn.fst.backend.backend.dto;

import java.util.List;

public class WishlistResponse {

    private Long wishlistId;
    private Long userId;
    private List<WishlistItemResponse> items;
    private Integer totalItems;
    private String createdAt;
    private String updatedAt;

    public WishlistResponse() {}

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private Long wishlistId;
        private Long userId;
        private List<WishlistItemResponse> items;
        private Integer totalItems;
        private String createdAt;
        private String updatedAt;

        public Builder wishlistId(Long wishlistId) {
            this.wishlistId = wishlistId;
            return this;
        }

        public Builder userId(Long userId) {
            this.userId = userId;
            return this;
        }

        public Builder items(List<WishlistItemResponse> items) {
            this.items = items;
            return this;
        }

        public Builder totalItems(Integer totalItems) {
            this.totalItems = totalItems;
            return this;
        }

        public Builder createdAt(String createdAt) {
            this.createdAt = createdAt;
            return this;
        }

        public Builder updatedAt(String updatedAt) {
            this.updatedAt = updatedAt;
            return this;
        }

        public WishlistResponse build() {
            WishlistResponse r = new WishlistResponse();
            r.wishlistId = wishlistId;
            r.userId = userId;
            r.items = items;
            r.totalItems = totalItems;
            r.createdAt = createdAt;
            r.updatedAt = updatedAt;
            return r;
        }
    }

    public Long getWishlistId() {
        return wishlistId;
    }

    public void setWishlistId(Long wishlistId) {
        this.wishlistId = wishlistId;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public List<WishlistItemResponse> getItems() {
        return items;
    }

    public void setItems(List<WishlistItemResponse> items) {
        this.items = items;
    }

    public Integer getTotalItems() {
        return totalItems;
    }

    public void setTotalItems(Integer totalItems) {
        this.totalItems = totalItems;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public String getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(String updatedAt) {
        this.updatedAt = updatedAt;
    }
}
