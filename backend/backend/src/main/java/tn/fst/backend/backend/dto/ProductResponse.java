package tn.fst.backend.backend.dto;

import tn.fst.backend.backend.entity.Product;

import java.time.LocalDateTime;
import java.util.List;

public class ProductResponse {

    private Long idProduct;
    private String name;
    private String description;
    private String color;
    private String size;
    private String imageUrl;
    private CategoryResponse category;
    private List<ProductVariantResponse> variants;
    private List<ProductImageResponse> images;
    private LocalDateTime createdAt;
    private Double rating;
    private java.math.BigDecimal price;
    private Integer stock;

    public ProductResponse() {}

    public ProductResponse(Long idProduct, String name, String description) {
        this.idProduct = idProduct;
        this.name = name;
        this.description = description;
    }

    public static ProductResponse fromEntity(Product product) {
        if (product == null) {
            return null;
        }
        ProductResponse response = new ProductResponse();
        response.setIdProduct(product.getIdProduct());
        response.setName(product.getName());
        response.setDescription(product.getDescription());

        if (product.getVariants() != null) {
            response.setVariants(product.getVariants().stream()
                    .map(v -> ProductVariantResponse.of(
                            v.getIdVariant(),
                            v.getSku(),
                            v.getSize(),
                            v.getColor(),
                            v.getImageUrl(),
                            v.getAdditionalPrice(),
                            v.getStockQuantity(),
                            v.getTotalPrice()))
                    .toList());
        }

        if (product.getImages() != null) {
            response.setImages(product.getImages().stream()
                    .map(img -> ProductImageResponse.of(
                            img.getIdImage(),
                            img.getImageUrl(),
                            img.getAltText(),
                            img.getIsPrimary(),
                            img.getDisplayOrder(),
                            img.getCreatedAt()))
                    .toList());
        }

        if (product.getCategory() != null) {
            var cat = product.getCategory();
            response.setCategory(CategoryResponse.of(cat.getIdCategory(), cat.getName(), cat.getDescription(), cat.getImage()));
        }
        response.setCreatedAt(product.getCreatedAt());
        response.setRating(product.getRating());
        response.setPrice(product.getPrice());
        response.setStock(product.getStock());
        response.setColor(product.getColor());
        response.setSize(product.getSize());
        response.setImageUrl(product.getImageUrl());
        return response;
    }

    public Long getIdProduct() {
        return idProduct;
    }

    public void setIdProduct(Long idProduct) {
        this.idProduct = idProduct;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public CategoryResponse getCategory() {
        return category;
    }

    public void setCategory(CategoryResponse category) {
        this.category = category;
    }

    public List<ProductVariantResponse> getVariants() {
        return variants;
    }

    public void setVariants(List<ProductVariantResponse> variants) {
        this.variants = variants;
    }

    public List<ProductImageResponse> getImages() {
        return images;
    }

    public void setImages(List<ProductImageResponse> images) {
        this.images = images;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public Double getRating() {
        return rating;
    }

    public void setRating(Double rating) {
        this.rating = rating;
    }

    public java.math.BigDecimal getPrice() {
        return price;
    }

    public void setPrice(java.math.BigDecimal price) {
        this.price = price;
    }

    public Integer getStock() {
        return stock;
    }

    public void setStock(Integer stock) {
        this.stock = stock;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public String getSize() {
        return size;
    }

    public void setSize(String size) {
        this.size = size;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }
}
