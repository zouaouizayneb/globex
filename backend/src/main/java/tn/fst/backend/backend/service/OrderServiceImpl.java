package tn.fst.backend.backend.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tn.fst.backend.backend.dto.*;
import tn.fst.backend.backend.entity.*;
import tn.fst.backend.backend.exeptions.ResourceNotFoundException;
import tn.fst.backend.backend.repository.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service de gestion des commandes
 * Gère: historique, suivi, annulation, retours, remboursements
 */
@Service
@RequiredArgsConstructor
@Transactional
public class OrderServiceImpl {

    private final OrderRepository orderRepository;
    private final OrderDetailRepository orderDetailRepository;
    private final PaymentRepository paymentRepository;
    private final ShipmentRepository shipmentRepository;
    private final UserRepository userRepository;
    private final ProductVariantRepository variantRepository;

    /**
     * Obtenir l'historique des commandes d'un client
     */
    @Transactional(readOnly = true)
    public List<OrderHistoryResponse> getOrderHistory(Long userId) {
        User user = getUserById(userId);
        List<Order> orders = orderRepository.findByUserOrderByDateOrderDesc(user);

        return orders.stream()
                .map(this::mapToOrderHistoryResponse)
                .collect(Collectors.toList());
    }

    /**
     * Obtenir les détails complets d'une commande
     */
    @Transactional(readOnly = true)
    public OrderDetailsResponse getOrderDetails(Long userId, Long orderId) {
        User user = getUserById(userId);
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order", orderId));

        // Vérifier que la commande appartient bien à l'utilisateur
        if (!order.getUser().getIdUser().equals(user.getIdUser())) {
            throw new IllegalArgumentException("Cette commande ne vous appartient pas");
        }

        return mapToOrderDetailsResponse(order);
    }

    /**
     * Suivre le statut d'une commande
     */
    @Transactional(readOnly = true)
    public OrderTrackingResponse trackOrder(Long userId, Long orderId) {
        User user = getUserById(userId);
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order", orderId));

        // Vérifier propriété
        if (!order.getUser().getIdUser().equals(user.getIdUser())) {
            throw new IllegalArgumentException("Cette commande ne vous appartient pas");
        }

        // Récupérer le shipment
        Shipment shipment = shipmentRepository.findByOrder(order)
                .orElse(null);

        // Récupérer le payment
        Payment payment = paymentRepository.findByOrder(order)
                .orElse(null);

        return OrderTrackingResponse.builder()
                .orderId(order.getIdOrder())
                .orderNumber(generateOrderNumber(order.getIdOrder()))
                .status(order.getStatus().name())
                .orderDate(order.getDateOrder())
                .totalAmount(order.getTotalAmount())
                .paymentStatus(payment != null ? payment.getStatus().name() : "UNKNOWN")
                .shipmentStatus(shipment != null ? shipment.getStatus().name() : "PENDING")
                .trackingNumber(shipment != null ? shipment.getTrackingNumber() : null)
                .estimatedDelivery(shipment != null ? shipment.getEstimatedDeliveryDate() : null)
                .canCancel(canCancelOrder(order))
                .canReturn(canReturnOrder(order))
                .build();
    }

    /**
     * Annuler une commande
     */
    public OrderCancellationResponse cancelOrder(Long userId, Long orderId, String reason) {
        User user = getUserById(userId);
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order", orderId));

        // Vérifier propriété
        if (!order.getUser().getIdUser().equals(user.getIdUser())) {
            throw new IllegalArgumentException("Cette commande ne vous appartient pas");
        }

        // Vérifier si l'annulation est possible
        if (!canCancelOrder(order)) {
            return OrderCancellationResponse.builder()
                    .success(false)
                    .message("Cette commande ne peut plus être annulée (statut: " + order.getStatus() + ")")
                    .build();
        }

        // Annuler la commande
        order.setStatus(OrderStatus.CANCELLED);
        orderRepository.save(order);

        // Restaurer le stock
        restoreStock(order);

        // Si payée, initier le remboursement
        Payment payment = paymentRepository.findByOrder(order).orElse(null);
        boolean refundInitiated = false;
        if (payment != null && payment.getStatus() == PaymentStatus.COMPLETED) {
            payment.setStatus(PaymentStatus.REFUNDED);
            paymentRepository.save(payment);
            refundInitiated = true;
        }

        return OrderCancellationResponse.builder()
                .success(true)
                .message("Commande annulée avec succès")
                .refundInitiated(refundInitiated)
                .refundAmount(refundInitiated ? order.getTotalAmount() : BigDecimal.ZERO)
                .build();
    }

    /**
     * Demander un retour de commande
     */
    public ReturnRequestResponse requestReturn(Long userId, Long orderId, ReturnRequest request) {
        User user = getUserById(userId);
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order", orderId));

        // Vérifier propriété
        if (!order.getUser().getIdUser().equals(user.getIdUser())) {
            throw new IllegalArgumentException("Cette commande ne vous appartient pas");
        }

        // Vérifier si le retour est possible
        if (!canReturnOrder(order)) {
            return ReturnRequestResponse.builder()
                    .success(false)
                    .message("Cette commande ne peut pas être retournée (délai dépassé ou statut incompatible)")
                    .build();
        }

        // Créer la demande de retour
        // TODO: Créer une entité ReturnRequest si nécessaire

        return ReturnRequestResponse.builder()
                .success(true)
                .message("Demande de retour enregistrée avec succès")
                .returnRequestId("RET-" + orderId + "-" + System.currentTimeMillis())
                .expectedRefundAmount(calculateRefundAmount(order))
                .build();
    }

    /**
     * Traiter un remboursement
     */
    public RefundResponse processRefund(Long orderId, String reason) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order", orderId));

        Payment payment = paymentRepository.findByOrder(order)
                .orElseThrow(() -> new ResourceNotFoundException("Payment not found for order"));

        // Vérifier que le paiement a été complété
        if (payment.getStatus() != PaymentStatus.COMPLETED) {
            return RefundResponse.builder()
                    .success(false)
                    .message("Le paiement n'a pas été complété, impossible de rembourser")
                    .build();
        }

        // Marquer comme remboursé
        payment.setStatus(PaymentStatus.REFUNDED);
        paymentRepository.save(payment);

        // Mettre à jour le statut de la commande
        order.setStatus(OrderStatus.REFUNDED);
        orderRepository.save(order);

        // Restaurer le stock
        restoreStock(order);

        return RefundResponse.builder()
                .success(true)
                .message("Remboursement traité avec succès")
                .refundAmount(payment.getAmount())
                .refundDate(LocalDate.now())
                .build();
    }

    /**
     * Obtenir les commandes par statut
     */
    @Transactional(readOnly = true)
    public List<OrderHistoryResponse> getOrdersByStatus(Long userId, OrderStatus status) {
        User user = getUserById(userId);
        List<Order> orders = orderRepository.findByUserAndStatus(user, status);

        return orders.stream()
                .map(this::mapToOrderHistoryResponse)
                .collect(Collectors.toList());
    }

    // ==================== HELPER METHODS ====================

    /**
     * Vérifier si une commande peut être annulée
     */
    private boolean canCancelOrder(Order order) {
        return order.getStatus() == OrderStatus.PENDING ||
                order.getStatus() == OrderStatus.CONFIRMED ||
                order.getStatus() == OrderStatus.PROCESSING;
    }

    /**
     * Vérifier si une commande peut être retournée
     */
    private boolean canReturnOrder(Order order) {
        // Peut être retournée si livrée et moins de 30 jours
        if (order.getStatus() != OrderStatus.DELIVERED) {
            return false;
        }

        Shipment shipment = shipmentRepository.findByOrder(order).orElse(null);
        if (shipment == null || shipment.getDeliveredAt() == null) {
            return false;
        }

        // Vérifier que moins de 30 jours se sont écoulés
        return shipment.getDeliveredAt().plusDays(30).isAfter(java.time.LocalDateTime.now());
    }

    /**
     * Restaurer le stock après annulation
     */
    private void restoreStock(Order order) {
        List<OrderDetail> details = orderDetailRepository.findByOrder(order);

        for (OrderDetail detail : details) {
            ProductVariant variant = detail.getVariant();
            variant.setStockQuantity(variant.getStockQuantity() + detail.getQuantity());
            variantRepository.save(variant);
        }
    }

    /**
     * Calculer le montant du remboursement
     */
    private BigDecimal calculateRefundAmount(Order order) {
        // Pour l'instant, remboursement total
        // Peut être ajusté selon les frais de retour, etc.
        return order.getTotalAmount();
    }

    /**
     * Mapper Order vers OrderHistoryResponse
     */
    private OrderHistoryResponse mapToOrderHistoryResponse(Order order) {
        Shipment shipment = shipmentRepository.findByOrder(order).orElse(null);
        Payment payment = paymentRepository.findByOrder(order).orElse(null);

        return OrderHistoryResponse.builder()
                .orderId(order.getIdOrder())
                .orderNumber(generateOrderNumber(order.getIdOrder()))
                .orderDate(order.getDateOrder())
                .status(order.getStatus().name())
                .totalAmount(order.getTotalAmount())
                .itemCount(orderDetailRepository.findByOrder(order).size())
                .paymentMethod(payment != null ? payment.getPaymentMethod().name() : "UNKNOWN")
                .shippingStatus(shipment != null ? shipment.getStatus().name() : "PENDING")
                .canCancel(canCancelOrder(order))
                .canReturn(canReturnOrder(order))
                .build();
    }

    /**
     * Mapper Order vers OrderDetailsResponse
     */
    private OrderDetailsResponse mapToOrderDetailsResponse(Order order) {
        List<OrderDetail> details = orderDetailRepository.findByOrder(order);
        Shipment shipment = shipmentRepository.findByOrder(order).orElse(null);
        Payment payment = paymentRepository.findByOrder(order).orElse(null);

        // Mapper les items
        List<OrderItemDetails> items = details.stream()
                .map(detail -> OrderItemDetails.builder()
                        .productName(detail.getVariant().getProduct().getName())
                        .variantDetails(detail.getVariant().getColor() + " - " + detail.getVariant().getSize())
                        .quantity(detail.getQuantity())
                        .unitPrice(detail.getPrice())
                        .subtotal(detail.getPrice().multiply(BigDecimal.valueOf(detail.getQuantity())))
                        .imageUrl(detail.getVariant().getProduct().getImages().isEmpty() ?
                                null : detail.getVariant().getProduct().getImages().get(0).getImageUrl())
                        .build())
                .collect(Collectors.toList());

        return OrderDetailsResponse.builder()
                .orderId(order.getIdOrder())
                .orderNumber(generateOrderNumber(order.getIdOrder()))
                .orderDate(order.getDateOrder())
                .status(order.getStatus().name())
                .items(items)
                .subtotal(calculateSubtotal(details))
                .shippingCost(shipment != null ? shipment.getShippingCost() : BigDecimal.ZERO)
                .taxAmount(BigDecimal.ZERO) // À calculer si stocké
                .totalAmount(order.getTotalAmount())
                .paymentMethod(payment != null ? payment.getPaymentMethod().name() : "UNKNOWN")
                .paymentStatus(payment != null ? payment.getStatus().name() : "UNKNOWN")
                .shippingAddress(shipment != null ? shipment.getShippingAddress().getFormattedAddress() : null)
                .trackingNumber(shipment != null ? shipment.getTrackingNumber() : null)
                .estimatedDelivery(shipment != null ? shipment.getEstimatedDeliveryDate() : null)
                .canCancel(canCancelOrder(order))
                .canReturn(canReturnOrder(order))
                .build();
    }

    private BigDecimal calculateSubtotal(List<OrderDetail> details) {
        return details.stream()
                .map(d -> d.getPrice().multiply(BigDecimal.valueOf(d.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private String generateOrderNumber(Long orderId) {
        return "ORD-" + String.format("%06d", orderId);
    }

    private User getUserById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", userId));
    }
}