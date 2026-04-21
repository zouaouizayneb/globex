package tn.fst.backend.backend.dto;

import java.math.BigDecimal;

public class OrderStatistics {

    private Long totalOrders;
    private BigDecimal totalRevenue;
    private Long pendingOrders;
    private Long completedOrders;
    private Long cancelledOrders;

    public OrderStatistics() {}

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private Long totalOrders;
        private BigDecimal totalRevenue;
        private Long pendingOrders;
        private Long completedOrders;
        private Long cancelledOrders;

        public Builder totalOrders(Long totalOrders) {
            this.totalOrders = totalOrders;
            return this;
        }

        public Builder totalRevenue(BigDecimal totalRevenue) {
            this.totalRevenue = totalRevenue;
            return this;
        }

        public Builder pendingOrders(Long pendingOrders) {
            this.pendingOrders = pendingOrders;
            return this;
        }

        public Builder completedOrders(Long completedOrders) {
            this.completedOrders = completedOrders;
            return this;
        }

        public Builder cancelledOrders(Long cancelledOrders) {
            this.cancelledOrders = cancelledOrders;
            return this;
        }

        public OrderStatistics build() {
            OrderStatistics r = new OrderStatistics();
            r.totalOrders = totalOrders;
            r.totalRevenue = totalRevenue;
            r.pendingOrders = pendingOrders;
            r.completedOrders = completedOrders;
            r.cancelledOrders = cancelledOrders;
            return r;
        }
    }

    public Long getTotalOrders() {
        return totalOrders;
    }

    public void setTotalOrders(Long totalOrders) {
        this.totalOrders = totalOrders;
    }

    public BigDecimal getTotalRevenue() {
        return totalRevenue;
    }

    public void setTotalRevenue(BigDecimal totalRevenue) {
        this.totalRevenue = totalRevenue;
    }

    public Long getPendingOrders() {
        return pendingOrders;
    }

    public void setPendingOrders(Long pendingOrders) {
        this.pendingOrders = pendingOrders;
    }

    public Long getCompletedOrders() {
        return completedOrders;
    }

    public void setCompletedOrders(Long completedOrders) {
        this.completedOrders = completedOrders;
    }

    public Long getCancelledOrders() {
        return cancelledOrders;
    }

    public void setCancelledOrders(Long cancelledOrders) {
        this.cancelledOrders = cancelledOrders;
    }
}
