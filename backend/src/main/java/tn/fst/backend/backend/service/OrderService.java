package tn.fst.backend.backend.service;

import tn.fst.backend.backend.entity.Order;
import tn.fst.backend.backend.entity.OrderStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface OrderService {

    // Get all orders with pagination
    Page<Order> getAllOrders(Pageable pageable);

    // Get all orders as list
    List<Order> getAllOrdersList();

    // Get order by ID
    Order getOrderById(Long id);

    // Create order (basic - without workflow)
    Order createOrder(Order order);

    // Update order
    Order updateOrder(Long id, Order orderDetails);

    // Delete order
    void deleteOrder(Long id);

    // Get orders by client
    List<Order> getOrdersByClient(Long clientId);

    // Get orders by status
    List<Order> getOrdersByStatus(Long userId, OrderStatus status);
}
