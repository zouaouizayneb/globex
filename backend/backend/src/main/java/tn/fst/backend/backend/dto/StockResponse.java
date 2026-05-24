package tn.fst.backend.backend.dto;

import tn.fst.backend.backend.entity.Stock;

public class StockResponse {

    private Long idStock;
    private Integer quantity;
    private ProductResponse product;
    private ProductVariantResponse variant;

    public StockResponse() {}

    public static StockResponse fromEntity(Stock stock) {
        if (stock == null) {
            return null;
        }
        
        StockResponse response = new StockResponse();
        response.setIdStock(stock.getIdStock());
        response.setQuantity(stock.getQuantity());
        
        if (stock.getProduct() != null) {
            response.setProduct(ProductResponse.fromEntity(stock.getProduct()));
        }
        
        if (stock.getVariant() != null) {
            response.setVariant(ProductVariantResponse.of(
                stock.getVariant().getIdVariant(),
                stock.getVariant().getSku(),
                stock.getVariant().getSize(),
                stock.getVariant().getColor(),
                stock.getVariant().getImageUrl(),
                stock.getVariant().getAdditionalPrice(),
                stock.getVariant().getStockQuantity(),
                stock.getVariant().getTotalPrice()
            ));
        }
        
        return response;
    }

    public Long getIdStock() {
        return idStock;
    }

    public void setIdStock(Long idStock) {
        this.idStock = idStock;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public ProductResponse getProduct() {
        return product;
    }

    public void setProduct(ProductResponse product) {
        this.product = product;
    }

    public ProductVariantResponse getVariant() {
        return variant;
    }

    public void setVariant(ProductVariantResponse variant) {
        this.variant = variant;
    }
}
