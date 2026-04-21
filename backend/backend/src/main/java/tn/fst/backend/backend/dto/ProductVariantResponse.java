package tn.fst.backend.backend.dto;

import java.math.BigDecimal;

public class ProductVariantResponse {

    private Long idVariant;
    private String sku;
    private String size;
    private String color;
    private String imageUrl;
    private BigDecimal additionalPrice;
    private Integer stockQuantity;
    private BigDecimal totalPrice;

    public ProductVariantResponse() {}

    public static ProductVariantResponse of(
            Long idVariant,
            String sku,
            String size,
            String color,
            String imageUrl,
            BigDecimal additionalPrice,
            Integer stockQuantity,
            BigDecimal totalPrice) {
        ProductVariantResponse r = new ProductVariantResponse();
        r.setIdVariant(idVariant);
        r.setSku(sku);
        r.setSize(size);
        r.setColor(color);
        r.setImageUrl(imageUrl);
        r.setAdditionalPrice(additionalPrice);
        r.setStockQuantity(stockQuantity);
        r.setTotalPrice(totalPrice);
        return r;
    }

    public Long getIdVariant() {
        return idVariant;
    }

    public void setIdVariant(Long idVariant) {
        this.idVariant = idVariant;
    }

    public String getSku() {
        return sku;
    }

    public void setSku(String sku) {
        this.sku = sku;
    }

    public String getSize() {
        return size;
    }

    public void setSize(String size) {
        this.size = size;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public BigDecimal getAdditionalPrice() {
        return additionalPrice;
    }

    public void setAdditionalPrice(BigDecimal additionalPrice) {
        this.additionalPrice = additionalPrice;
    }

    public Integer getStockQuantity() {
        return stockQuantity;
    }

    public void setStockQuantity(Integer stockQuantity) {
        this.stockQuantity = stockQuantity;
    }

    public BigDecimal getTotalPrice() {
        return totalPrice;
    }

    public void setTotalPrice(BigDecimal totalPrice) {
        this.totalPrice = totalPrice;
    }
}
