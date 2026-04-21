package tn.fst.backend.backend.dto;

import tn.fst.backend.backend.entity.OrderStatus;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public class OrderResponse {

    private Long orderId;
    private Long clientId;
    private String clientName;
    private LocalDate orderDate;
    private OrderStatus status;
    private BigDecimal totalAmount;
    private String invoiceNumber;
    private List<OrderItemResponse> items;

    public OrderResponse() {}

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private Long orderId;
        private Long clientId;
        private String clientName;
        private LocalDate orderDate;
        private OrderStatus status;
        private BigDecimal totalAmount;
        private String invoiceNumber;
        private List<OrderItemResponse> items;

        public Builder orderId(Long orderId) {
            this.orderId = orderId;
            return this;
        }

        public Builder clientId(Long clientId) {
            this.clientId = clientId;
            return this;
        }

        public Builder clientName(String clientName) {
            this.clientName = clientName;
            return this;
        }

        public Builder orderDate(LocalDate orderDate) {
            this.orderDate = orderDate;
            return this;
        }

        public Builder status(OrderStatus status) {
            this.status = status;
            return this;
        }

        public Builder totalAmount(BigDecimal totalAmount) {
            this.totalAmount = totalAmount;
            return this;
        }

        public Builder invoiceNumber(String invoiceNumber) {
            this.invoiceNumber = invoiceNumber;
            return this;
        }

        public Builder items(List<OrderItemResponse> items) {
            this.items = items;
            return this;
        }

        public OrderResponse build() {
            OrderResponse r = new OrderResponse();
            r.orderId = orderId;
            r.clientId = clientId;
            r.clientName = clientName;
            r.orderDate = orderDate;
            r.status = status;
            r.totalAmount = totalAmount;
            r.invoiceNumber = invoiceNumber;
            r.items = items;
            return r;
        }
    }

    public Long getOrderId() {
        return orderId;
    }

    public void setOrderId(Long orderId) {
        this.orderId = orderId;
    }

    public Long getClientId() {
        return clientId;
    }

    public void setClientId(Long clientId) {
        this.clientId = clientId;
    }

    public String getClientName() {
        return clientName;
    }

    public void setClientName(String clientName) {
        this.clientName = clientName;
    }

    public LocalDate getOrderDate() {
        return orderDate;
    }

    public void setOrderDate(LocalDate orderDate) {
        this.orderDate = orderDate;
    }

    public OrderStatus getStatus() {
        return status;
    }

    public void setStatus(OrderStatus status) {
        this.status = status;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }

    public String getInvoiceNumber() {
        return invoiceNumber;
    }

    public void setInvoiceNumber(String invoiceNumber) {
        this.invoiceNumber = invoiceNumber;
    }

    public List<OrderItemResponse> getItems() {
        return items;
    }

    public void setItems(List<OrderItemResponse> items) {
        this.items = items;
    }
}
