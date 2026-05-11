package tn.fst.backend.backend.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
@Slf4j
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final OrderDetailRepository orderDetailRepository;
    private final PaymentRepository paymentRepository;
    private final ShipmentRepository shipmentRepository;
    private final UserRepository userRepository;
    private final ProductVariantRepository variantRepository;
    private final ProductRepository productRepository;
    private final StockRepository stockRepository;
    private final ClientRepository clientRepository;
    private final TransporteurRepository transporteurRepository;
    private final InvoiceService invoiceService;
    private final EmailService emailService;
    private final NotificationService notificationService;
    private final InventoryService inventoryService;

    @Override
    @Transactional(readOnly = true)
    public org.springframework.data.domain.Page<Order> getAllOrders(org.springframework.data.domain.Pageable pageable) {
        return orderRepository.findAll(pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Order> getAllOrdersList() {
        return orderRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public Order getOrderById(Long id) {
        return orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Order", id));
    }

    @Override
    public Order createOrder(Order order) {
        return orderRepository.save(order);
    }

    @Override
    public Order updateOrder(Long id, Order orderDetails) {
        Order existing = getOrderById(id);
        existing.setStatus(orderDetails.getStatus());
        existing.setTotalAmount(orderDetails.getTotalAmount());
        return orderRepository.save(existing);
    }

    @Override
    public void deleteOrder(Long id) {
        orderRepository.deleteById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Order> getOrdersByClient(Long clientId) {
        return orderRepository.findByClient_IdClient(clientId);
    }

    /**
     * Obtenir l'historique des commandes d'un client
     */
    @Override
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
    @Override
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
    @Override
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
    @Override
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
        order = orderRepository.save(order);

        // Send notification
        emailService.sendOrderStatusEmail(order);

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
    @Override
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
    @Override
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
        order = orderRepository.save(order);

        // Send notification
        emailService.sendOrderStatusEmail(order);

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
    @Override
    public List<Order> getOrdersByStatus(Long userId, OrderStatus status) {
        User user = getUserById(userId);
        return orderRepository.findByUserAndStatus(user, status);
    }

    @Override
    public Order createFrontendOrder(Long userId, tn.fst.backend.backend.dto.FrontendOrderRequest request) {
        User user = null;
        if (userId != null) {
            user = userRepository.findById(userId).orElse(null);
        }

        // If not found by ID (or userId was null), try by email from request
        if (user == null && request.getEmail() != null) {
            user = userRepository.findByEmail(request.getEmail()).orElse(null);
        }

        Client client = null;
        if (user != null) {
            client = clientRepository.findByUser(user).orElse(null);

            // Link address/country to client if missing
            if (client != null) {
                if (client.getAddress() == null) client.setAddress(request.getShippingAddress());
                if (client.getCountry() == null) client.setCountry(request.getCountry());
                clientRepository.save(client);
            } else {
                // Create a client record for this user
                client = new Client();
                client.setUser(user);
                client.setName(user.getFullname());
                client.setCountry(request.getCountry());
                client.setAddress(request.getShippingAddress());
                client = clientRepository.save(client);
            }
        }

        // Build the Order — now including shipping/tax breakdown and address
        Order order = Order.builder()
                .user(user)
                .client(client)
                .dateOrder(LocalDate.now())
                .status(OrderStatus.PENDING)
                .totalAmount(request.getTotal() != null ? request.getTotal() : BigDecimal.ZERO)
                .shippingCost(request.getShippingCost() != null ? request.getShippingCost() : BigDecimal.ZERO)
                .taxAmount(request.getTaxes() != null ? request.getTaxes() : BigDecimal.ZERO)
                .shippingAddress(request.getShippingAddress())
                .shippingMethod(request.getShippingMethod())
                .paymentMethodLabel(request.getPaymentMethod())
                .build();

        order = orderRepository.save(order);

        // Create order details — throw if a product can't be resolved (never silently drop)
        if (request.getItems() != null) {
            for (tn.fst.backend.backend.dto.FrontendOrderItem item : request.getItems()) {
                // First try to find by variant ID, then fall back to product ID
                ProductVariant variant = variantRepository.findById(item.getProductId()).orElse(null);
                Product product = null;

                if (variant != null) {
                    product = variant.getProduct();
                } else {
                    // productId is a Product ID — find the product and pick its first variant
                    product = productRepository.findById(item.getProductId())
                            .orElseThrow(() -> new ResourceNotFoundException("Product", item.getProductId()));
                    if (!product.getVariants().isEmpty()) {
                        variant = product.getVariants().get(0);
                    }
                }

                OrderDetail detail = OrderDetail.builder()
                        .order(order)
                        .product(product)
                        .variant(variant)
                        .quantity(item.getQuantity())
                        .price(item.getUnitPrice())
                        .build();

                orderDetailRepository.save(detail);

                // Deduct stock if variant is present and log stock movement
                if (variant != null && variant.getStockQuantity() != null) {
                    variant.setStockQuantity(Math.max(0, variant.getStockQuantity() - item.getQuantity()));
                    variantRepository.save(variant);

                    stockRepository.findByVariant(variant).ifPresent(stock -> {
                        stock.setQuantity(Math.max(0, stock.getQuantity() - item.getQuantity()));
                        stockRepository.save(stock);
                    });

                    // Fix 2: Record stock movement history (SALE type)
                    try {
                        inventoryService.recordSale(variant.getIdVariant(), item.getQuantity(), order.getIdOrder());
                        log.info("Stock movement recorded for variant #{} (qty: {})", variant.getIdVariant(), item.getQuantity());
                    } catch (Exception e) {
                        log.warn("Failed to record stock movement for variant #{}: {}", variant.getIdVariant(), e.getMessage());
                    }
                }
            }
        }

        // Create a Payment record linked to the order
        Payment payment = Payment.builder()
                .order(order)
                .amount(order.getTotalAmount())
                .status(PaymentStatus.PENDING)
                .datePayment(LocalDate.now())
                .modePayment(request.getPaymentMethod())
                .build();

        // Map the payment method string to the enum (now includes D17, FLOUCI)
        if (request.getPaymentMethod() != null) {
            try {
                payment.setPaymentMethod(PaymentMethod.valueOf(request.getPaymentMethod().toUpperCase()));
            } catch (IllegalArgumentException e) {
                // Log unrecognised method — store it only as modePayment string
                // This prevents the whole checkout from crashing on unknown methods
                payment.setModePayment(request.getPaymentMethod());
            }
        }

        paymentRepository.saveAndFlush(payment);
        log.info("Payment record created for order #{}", order.getIdOrder());

        // Fix 1 & 3: Automation — create invoice record AND generate PDF immediately
        try {
            InvoiceResponse invoiceResp = invoiceService.issueDate(order.getIdOrder());
            log.info("Invoice automatically generated for order #{}", order.getIdOrder());
            // Generate the PDF immediately so it's available for download
            try {
                invoiceService.ensureInvoicePdf(invoiceResp.getIdInvoice());
                log.info("Invoice PDF generated for invoice #{}", invoiceResp.getIdInvoice());
            } catch (Exception pdfEx) {
                log.warn("Invoice PDF generation failed for invoice #{}: {}", invoiceResp.getIdInvoice(), pdfEx.getMessage());
            }
        } catch (Exception e) {
            log.error("Failed to automatically generate invoice for order #{}: {}", order.getIdOrder(), e.getMessage());
            // Keep checkout/sale flow successful even if invoice generation fails.
        }

        // Fix 4: Notify all admins — in-app notification + email
        try {
            List<User> admins = userRepository.findByRole(Role.ADMIN);
            String orderMsg = "Nouvelle commande #" + order.getIdOrder()
                    + " — " + order.getTotalAmount() + " TND";
            for (User admin : admins) {
                // In-app DB notification
                Notification notif = new Notification();
                notif.setType("NEW_ORDER");
                notif.setMessage(orderMsg);
                notif.setStatus(Notification.Status.UNREAD);
                notif.setDateSend(LocalDate.now());
                notif.setUser(admin);
                notificationService.createNotification(notif);
                // Admin email
                emailService.sendNewOrderAdminEmail(order, admin.getEmail());
            }
            log.info("Admin notifications sent for order #{} to {} admin(s)", order.getIdOrder(), admins.size());
        } catch (Exception e) {
            log.warn("Failed to send admin notifications for order #{}: {}", order.getIdOrder(), e.getMessage());
        }

        return order;
    }

    @Override
    public Order adminUpdateOrder(Long orderId, OrderStatus status, Transporteur transporteur) {
        log.info("Admin update order #{} - New status: {}, Transporteur: {}", orderId, status, transporteur != null ? transporteur.getName() : "none");
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order", orderId));

        if (status != null) {
            order.setStatus(status);
        }

        Shipment shipment = shipmentRepository.findByOrder(order).orElse(null);
        if (transporteur != null) {
            order.setTransporteur(transporteur);
            if (shipment == null) {
                shipment = Shipment.builder()
                        .order(order)
                        .status(order.getStatus())
                        .dateShip(LocalDate.now())
                        .build();
            }
            shipment.setCarrier(transporteur.getName());
        }

        if (shipment != null && order.getStatus() != null) {
            shipment.setStatus(order.getStatus());
            shipmentRepository.save(shipment);
        }

        order = orderRepository.save(order);
        log.info("Order #{} saved successfully with status {}", orderId, order.getStatus());

        // Send notification
        emailService.sendOrderStatusEmail(order);

        return order;
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
            if (variant != null) {
                variant.setStockQuantity(variant.getStockQuantity() + detail.getQuantity());
                variantRepository.save(variant);

                stockRepository.findByVariant(variant)
                        .ifPresent(stock -> {
                            stock.setQuantity(stock.getQuantity() + detail.getQuantity());
                            stockRepository.save(stock);
                        });
            }
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

        // Resolve payment method label safely — avoid NPE when enum value is null
        String paymentMethodLabel = "UNKNOWN";
        if (payment != null) {
            if (payment.getPaymentMethod() != null) {
                paymentMethodLabel = payment.getPaymentMethod().name();
            } else if (payment.getModePayment() != null) {
                paymentMethodLabel = payment.getModePayment();
            }
        }

        return OrderHistoryResponse.builder()
                .orderId(order.getIdOrder())
                .orderNumber(generateOrderNumber(order.getIdOrder()))
                .orderDate(order.getDateOrder())
                .status(order.getStatus().name())
                .totalAmount(order.getTotalAmount())
                .itemCount(orderDetailRepository.findByOrder(order).size())
                .paymentMethod(paymentMethodLabel)
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

        // Map order items — guard against missing variant or product (guest-checkout items)
        List<OrderItemDetails> items = details.stream()
                .map(detail -> {
                    String productName = "Unknown Product";
                    String variantDetails = "";
                    String imageUrl = null;

                    if (detail.getVariant() != null && detail.getVariant().getProduct() != null) {
                        Product p = detail.getVariant().getProduct();
                        productName = p.getName();
                        variantDetails = detail.getVariant().getColor() + " - " + detail.getVariant().getSize();
                        imageUrl = (p.getImages() != null && !p.getImages().isEmpty())
                                ? p.getImages().get(0).getImageUrl() : null;
                    } else if (detail.getProduct() != null) {
                        productName = detail.getProduct().getName();
                        imageUrl = (detail.getProduct().getImages() != null && !detail.getProduct().getImages().isEmpty())
                                ? detail.getProduct().getImages().get(0).getImageUrl() : null;
                    }

                    return OrderItemDetails.builder()
                            .productName(productName)
                            .variantDetails(variantDetails)
                            .quantity(detail.getQuantity())
                            .unitPrice(detail.getPrice())
                            .subtotal(detail.getPrice().multiply(BigDecimal.valueOf(detail.getQuantity())))
                            .imageUrl(imageUrl)
                            .build();
                })
                .collect(Collectors.toList());

        // Resolve payment method label safely
        String paymentMethodLabel = "UNKNOWN";
        if (payment != null) {
            if (payment.getPaymentMethod() != null) {
                paymentMethodLabel = payment.getPaymentMethod().name();
            } else if (payment.getModePayment() != null) {
                paymentMethodLabel = payment.getModePayment();
            }
        }

        // Use the stored shippingCost / taxAmount from the Order itself when no Shipment exists yet
        BigDecimal shippingCost = shipment != null ? shipment.getShippingCost()
                : (order.getShippingCost() != null ? order.getShippingCost() : BigDecimal.ZERO);
        BigDecimal taxAmount = order.getTaxAmount() != null ? order.getTaxAmount() : BigDecimal.ZERO;

        // Resolve shipping address
        String shippingAddress = null;
        if (shipment != null && shipment.getShippingAddress() != null) {
            shippingAddress = shipment.getShippingAddress().getFormattedAddress();
        } else if (order.getShippingAddress() != null) {
            shippingAddress = order.getShippingAddress();
        }

        return OrderDetailsResponse.builder()
                .orderId(order.getIdOrder())
                .orderNumber(generateOrderNumber(order.getIdOrder()))
                .orderDate(order.getDateOrder())
                .status(order.getStatus().name())
                .items(items)
                .subtotal(calculateSubtotal(details))
                .shippingCost(shippingCost)
                .taxAmount(taxAmount)
                .totalAmount(order.getTotalAmount())
                .paymentMethod(paymentMethodLabel)
                .paymentStatus(payment != null ? payment.getStatus().name() : "UNKNOWN")
                .shippingAddress(shippingAddress)
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