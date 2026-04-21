package tn.fst.backend.backend.exeptions;

public class InsufficientStockException extends RuntimeException {

    private final String productName;
    private final Integer requested;
    private final Integer available;

    public InsufficientStockException(String productName, Integer requested, Integer available) {
        super(String.format("Insufficient stock for %s. Requested: %d, Available: %d",
                productName, requested, available));
        this.productName = productName;
        this.requested = requested;
        this.available = available;
    }

    // Getters
    public String getProductName() {
        return productName;
    }

    public Integer getRequested() {
        return requested;
    }

    public Integer getAvailable() {
        return available;
    }
}