package tn.fst.backend.backend.dto;

import java.time.LocalDate;

public class ProductImageResponse {

    private Long idImage;
    private String imageUrl;
    private String altText;
    private Boolean isPrimary;
    private Integer displayOrder;
    private LocalDate createdAt;

    public ProductImageResponse() {}

    public static ProductImageResponse of(
            Long idImage,
            String imageUrl,
            String altText,
            Boolean isPrimary,
            Integer displayOrder,
            LocalDate createdAt) {
        ProductImageResponse r = new ProductImageResponse();
        r.setIdImage(idImage);
        r.setImageUrl(imageUrl);
        r.setAltText(altText);
        r.setIsPrimary(isPrimary);
        r.setDisplayOrder(displayOrder);
        r.setCreatedAt(createdAt);
        return r;
    }

    public Long getIdImage() {
        return idImage;
    }

    public void setIdImage(Long idImage) {
        this.idImage = idImage;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getAltText() {
        return altText;
    }

    public void setAltText(String altText) {
        this.altText = altText;
    }

    public Boolean getIsPrimary() {
        return isPrimary;
    }

    public void setIsPrimary(Boolean isPrimary) {
        this.isPrimary = isPrimary;
    }

    public Integer getDisplayOrder() {
        return displayOrder;
    }

    public void setDisplayOrder(Integer displayOrder) {
        this.displayOrder = displayOrder;
    }

    public LocalDate getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDate createdAt) {
        this.createdAt = createdAt;
    }
}
