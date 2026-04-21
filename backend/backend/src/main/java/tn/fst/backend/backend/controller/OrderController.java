package tn.fst.backend.backend.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import tn.fst.backend.backend.dto.*;
import tn.fst.backend.backend.entity.Order;
import tn.fst.backend.backend.entity.OrderStatus;
import tn.fst.backend.backend.entity.User;
import tn.fst.backend.backend.service.OrderService;

import java.util.List;

@RestController
@RequestMapping("/api/orders")
@CrossOrigin(origins = "http://localhost:4200")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

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
    public ResponseEntity<Order> createFrontendOrder(
            @RequestBody FrontendOrderRequest request,
            Authentication authentication) {
        Long userId = getCurrentUserId(authentication);
        Order order = orderService.createFrontendOrder(userId, request);
        return ResponseEntity.ok(order);
    }

    private Long getCurrentUserId(Authentication authentication) {
        if (authentication == null || authentication.getPrincipal() == null) {
            return null; // or throw unauthorized
        }
        User user = (User) authentication.getPrincipal();
        return user.getIdUser();
    }
}