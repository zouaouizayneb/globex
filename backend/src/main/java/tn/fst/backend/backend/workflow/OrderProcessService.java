package tn.fst.backend.backend.workflow;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import tn.fst.backend.backend.dto.OrderRequest;
import tn.fst.backend.backend.dto.OrderResponse;
import tn.fst.backend.backend.entity.OrderStatus;

public interface OrderProcessService {

    // Place a new order with full workflow
    OrderResponse placeOrder(OrderRequest request);

    // Update order status
    OrderResponse updateOrderStatus(Long orderId, OrderStatus newStatus);

    // Cancel order and restore stock
    OrderResponse cancelOrder(Long orderId);

    // Get order details with full response
    OrderResponse getOrderById(Long orderId);

    // Get all orders with full response and pagination
    Page<OrderResponse> getAllOrders(Pageable pageable);
}