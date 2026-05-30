package tn.fst.backend.backend.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import tn.fst.backend.backend.dto.*;
import tn.fst.backend.backend.entity.Order;
import tn.fst.backend.backend.entity.OrderStatus;
import tn.fst.backend.backend.entity.Transporteur;
import tn.fst.backend.backend.entity.User;
import tn.fst.backend.backend.exeptions.ResourceNotFoundException;
import tn.fst.backend.backend.repository.TransporteurRepository;
import tn.fst.backend.backend.service.OrderService;

import java.util.List;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "http://localhost:4200")
public class OrderController {

    private final OrderService orderService;
    private final TransporteurRepository transporteurRepository;

    @GetMapping("/all")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<OrderHistoryResponse>> getAllOrders() {
        List<Order> orders = orderService.getAllOrdersList();
        List<OrderHistoryResponse> response = orders.stream()
                .map(order -> {
                    String customerName = "Unknown";
                    String customerEmail = "N/A";
                    if (order.getClient() != null) {
                        customerName = order.getClient().getName();
                        if (order.getClient().getUser() != null) {
                            customerEmail = order.getClient().getUser().getEmail();
                        }
                    } else if (order.getUser() != null) {
                        customerName = order.getUser().getFullname();
                        customerEmail = order.getUser().getEmail();
                    }
                    return OrderHistoryResponse.builder()
                        .orderId(order.getIdOrder())
                        .orderNumber("ORD-" + String.format("%06d", order.getIdOrder()))
                        .orderDate(order.getDateOrder())
                        .status(order.getStatus().name())
                        .totalAmount(order.getTotalAmount())
                        .itemCount(order.getOrderDetails() != null ? order.getOrderDetails().size() : 0)
                        .paymentMethod(order.getPaymentMethodLabel() != null ? order.getPaymentMethodLabel() : "UNKNOWN")
                        .shippingStatus("PENDING")
                        .canCancel(false)
                        .canReturn(false)
                        .customerName(customerName)
                        .customerEmail(customerEmail)
                        .build();
                })
                .collect(java.util.stream.Collectors.toList());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/history")
    public ResponseEntity<List<OrderHistoryResponse>> getOrderHistory(
            Authentication authentication) {

        Long userId = getCurrentUserId(authentication);
        List<OrderHistoryResponse> orders = orderService.getOrderHistory(userId);
        return ResponseEntity.ok(orders);
    }

    @GetMapping("/{orderId}")
    public ResponseEntity<OrderDetailsResponse> getOrderDetails(
            @PathVariable Long orderId,
            Authentication authentication) {

        Long userId = getCurrentUserId(authentication);
        OrderDetailsResponse details = orderService.getOrderDetails(userId, orderId);
        return ResponseEntity.ok(details);
    }

    @GetMapping("/{orderId}/track")
    public ResponseEntity<OrderTrackingResponse> trackOrder(
            @PathVariable Long orderId,
            Authentication authentication) {

        Long userId = getCurrentUserId(authentication);
        OrderTrackingResponse tracking = orderService.trackOrder(userId, orderId);
        return ResponseEntity.ok(tracking);
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<List<Order>> getOrdersByStatus(
            @PathVariable String status,
            Authentication authentication) {

        Long userId = getCurrentUserId(authentication);
        OrderStatus orderStatus = OrderStatus.valueOf(status.toUpperCase());
        List<Order> orders = orderService.getOrdersByStatus(userId, orderStatus);
        return ResponseEntity.ok(orders);
    }

    @PostMapping("/{orderId}/cancel")
    public ResponseEntity<OrderCancellationResponse> cancelOrder(
            @PathVariable Long orderId,
            @Valid @RequestBody OrderCancellationRequest request,
            Authentication authentication) {

        Long userId = getCurrentUserId(authentication);
        OrderCancellationResponse response = orderService.cancelOrder(userId, orderId, request.getReason());
        return ResponseEntity.ok(response);
    }

    @PostMapping("/create")
    public ResponseEntity<OrderCreatedResponse> createFrontendOrder(
            @RequestBody FrontendOrderRequest request,
            Authentication authentication) {
        log.info("POST createFrontendOrder hit - Authentication: {}", authentication != null ? authentication.getName() : "null");
        try {
            Long userId = getCurrentUserId(authentication);
            log.info("DEBUG: OrderController - Derived userId: {}", userId);
            Order order = orderService.createFrontendOrder(userId, request);
            OrderCreatedResponse response = OrderCreatedResponse.builder()
                    .id(order.getIdOrder())
                    .orderNumber("ORD-" + String.format("%06d", order.getIdOrder()))
                    .status(order.getStatus().name())
                    .message("Your order has been placed successfully.")
                    .build();
            log.info("Order created successfully, returning response");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error creating order: ", e);
            throw e;
        }
    }

    @PatchMapping("/{orderId}/admin-update")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Order> adminUpdateOrder(
            @PathVariable Long orderId,
            @RequestBody AdminOrderUpdateRequest request) {
        log.info("PATCH admin-update for order #{} - status: {}, transporteurId: {}", orderId, request.getStatus(), request.getTransporteurId());
        
        OrderStatus status = request.getStatus() != null
                ? OrderStatus.valueOf(request.getStatus().toUpperCase())
                : null;

        Transporteur transporteur = null;
        if (request.getTransporteurId() != null) {
            transporteur = transporteurRepository.findById(request.getTransporteurId())
                    .orElseThrow(() -> new ResourceNotFoundException("Transporteur", request.getTransporteurId()));
        }

        Order updated = orderService.adminUpdateOrder(orderId, status, transporteur);
        return ResponseEntity.ok(updated);
    }

    @PutMapping("/{orderId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Order> updateOrderStatus(
            @PathVariable Long orderId,
            @RequestBody java.util.Map<String, Object> request) {
        log.info("PUT update status for order #{} - request: {}", orderId, request);
        
        Object statusObj = request.get("status");
        String statusStr = statusObj != null ? statusObj.toString() : null;
        
        OrderStatus status = statusStr != null ? OrderStatus.valueOf(statusStr.toUpperCase()) : null;
        Order updated = orderService.adminUpdateOrder(orderId, status, null);
        return ResponseEntity.ok(updated);
    }

    private Long getCurrentUserId(Authentication authentication) {
        if (authentication == null || authentication.getPrincipal() == null) {
            log.error("Authentication is null or principal is null");
            throw new RuntimeException("User not authenticated");
        }
        User user = (User) authentication.getPrincipal();
        log.info("Successfully retrieved user ID: {} for user: {}", user.getIdUser(), user.getUsername());
        return user.getIdUser();
    }
}