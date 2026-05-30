package tn.fst.backend.backend.dto;

import java.util.List;

public class ProductRequest {
    private String name;
    private String description;
    private Double price;
    private CategoryDto category;
    private SupplierDto supplier;
    private String status;
    private List<VariantDto> variants;

    public ProductRequest() {}

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

    public CategoryDto getCategory() {
        return category;
    }

    public void setCategory(CategoryDto category) {
        this.category = category;
    }

    public SupplierDto getSupplier() {
        return supplier;
    }

    public void setSupplier(SupplierDto supplier) {
        this.supplier = supplier;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public List<VariantDto> getVariants() {
        return variants;
    }

    public void setVariants(List<VariantDto> variants) {
        this.variants = variants;
    }

    public Long getCategoryId() {
        return category != null ? category.getIdCategory() : null;
    }

    public Long getSupplierId() {
        return supplier != null ? supplier.getIdSupplier() : null;
    }

    public static class CategoryDto {
        private Long idCategory;

        public Long getIdCategory() {
            return idCategory;
        }

        public void setIdCategory(Long idCategory) {
            this.idCategory = idCategory;
        }
    }

    public static class SupplierDto {
        private Long idSupplier;

        public Long getIdSupplier() {
            return idSupplier;
        }

        public void setIdSupplier(Long idSupplier) {
            this.idSupplier = idSupplier;
        }
    }

    public static class VariantDto {
        private String sku;
        private Double additionalPrice;
        private String color;
        private String size;
        private Integer stockQuantity;
        private List<ImageDto> images;

        public String getSku() {
            return sku;
        }

        public void setSku(String sku) {
            this.sku = sku;
        }

        public Double getAdditionalPrice() {
            return additionalPrice;
        }

        public void setAdditionalPrice(Double additionalPrice) {
            this.additionalPrice = additionalPrice;
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

        public Integer getStockQuantity() {
            return stockQuantity;
        }

        public void setStockQuantity(Integer stockQuantity) {
            this.stockQuantity = stockQuantity;
        }

        public List<ImageDto> getImages() {
            return images;
        }

        public void setImages(List<ImageDto> images) {
            this.images = images;
        }
    }

    public static class ImageDto {
        private String imageUrl;
        private Boolean isPrimary;

        public String getImageUrl() {
            return imageUrl;
        }

        public void setImageUrl(String imageUrl) {
            this.imageUrl = imageUrl;
        }

        public Boolean getIsPrimary() {
            return isPrimary;
        }

        public void setIsPrimary(Boolean isPrimary) {
            this.isPrimary = isPrimary;
        }
    }
}
