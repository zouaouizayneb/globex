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
@CrossOrigin(origins = "http://localhost:4200")
@RequiredArgsConstructor
@Slf4j
public class OrderController {

    private final OrderService orderService;
    private final TransporteurRepository transporteurRepository;

    @GetMapping("/all")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<Order>> getAllOrders() {
        List<Order> orders = orderService.getAllOrdersList();
        return ResponseEntity.ok(orders);
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

    @PostMapping
    public ResponseEntity<OrderCreatedResponse> createFrontendOrder(
            @RequestBody FrontendOrderRequest request,
            Authentication authentication) {
        Long userId = getCurrentUserId(authentication);
        Order order = orderService.createFrontendOrder(userId, request);
        OrderCreatedResponse response = OrderCreatedResponse.builder()
                .id(order.getIdOrder())
                .orderNumber("ORD-" + String.format("%06d", order.getIdOrder()))
                .status(order.getStatus().name())
                .message("Your order has been placed successfully.")
                .build();
        return ResponseEntity.ok(response);
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
            return null; // or throw unauthorized
        }
        User user = (User) authentication.getPrincipal();
        return user.getIdUser();
    }
}