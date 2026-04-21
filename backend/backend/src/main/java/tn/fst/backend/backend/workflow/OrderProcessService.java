package tn.fst.backend.backend.workflow;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import tn.fst.backend.backend.dto.OrderRequest;
import tn.fst.backend.backend.dto.OrderResponse;
import tn.fst.backend.backend.entity.OrderStatus;

/**
 * Order placement and lifecycle workflow.
 */
public interface OrderProcessService {

    OrderResponse placeOrder(OrderRequest request);

    OrderResponse updateOrderStatus(Long orderId, OrderStatus newStatus);

    OrderResponse cancelOrder(Long orderId);

    OrderResponse getOrderById(Long orderId);

    Page<OrderResponse> getAllOrders(Pageable pageable);
}
