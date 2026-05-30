package tn.fst.backend.backend.dto;

import tn.fst.backend.backend.entity.ProductStatus;
import tn.fst.backend.backend.entity.Product;
import tn.fst.backend.backend.dto.CategoryResponse;
import tn.fst.backend.backend.dto.ProductVariantResponse;
import tn.fst.backend.backend.dto.ProductImageResponse;

import java.time.LocalDateTime;
import java.util.List;

public class ProductResponse {

    private Long idProduct;
    private String name;
    private String description;
    private Double price;
    private CategoryResponse category;
    private List<ProductVariantResponse> variants;
    private List<ProductImageResponse> images;
    private LocalDateTime createdAt;
    private Double rating;
    private Long supplierId;
    private ProductStatus status;

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

        // Use product price if available, otherwise calculate from minimum variant price
        if (product.getPrice() != null && product.getPrice().doubleValue() > 0) {
            response.setPrice(product.getPrice().doubleValue());
        } else if (product.getVariants() != null && !product.getVariants().isEmpty()) {
            Double minPrice = product.getVariants().stream()
                    .map(v -> v.getTotalPrice() != null ? v.getTotalPrice().doubleValue() : 0.0)
                    .min(Double::compare)
                    .orElse(0.0);
            response.setPrice(minPrice);
        } else {
            response.setPrice(0.0);
        }

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
                            img.getCreatedAt(),
                            img.getVariant() != null ? img.getVariant().getIdVariant() : null))
                    .toList());
        }

        if (product.getCategory() != null) {
            var cat = product.getCategory();
            response.setCategory(CategoryResponse.of(cat.getIdCategory(), cat.getName(), cat.getDescription(), cat.getImage()));
        }
        response.setCreatedAt(product.getCreatedAt());
        response.setRating(product.getRating());
        response.setSupplierId(product.getSupplier() != null ? product.getSupplier().getIdSupplier() : null);
        response.setStatus(product.getStatus());
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

    public Double getPrice() {
        return price;
    }

    public void setPrice(Double price) {
        this.price = price;
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

    public Long getSupplierId() {
        return supplierId;
    }

    public void setSupplierId(Long supplierId) {
        this.supplierId = supplierId;
    }

    public ProductStatus getStatus() {
        return status;
    }

    public void setStatus(ProductStatus status) {
        this.status = status;
    }
}
