package tn.fst.backend.backend.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tn.fst.backend.backend.dto.*;
import tn.fst.backend.backend.entity.*;
import tn.fst.backend.backend.exeptions.InsufficientStockException;
import tn.fst.backend.backend.exeptions.ResourceNotFoundException;
import tn.fst.backend.backend.repository.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class CheckoutService {

    private final CartService cartService;
    private final AddressService addressService;
    private final TaxService taxService;
    private final ShippingService shippingService;
    private final PaymentService paymentService;

    private final CartRepository cartRepository;
    private final OrderRepository orderRepository;
    private final OrderDetailRepository orderDetailRepository;
    private final ProductVariantRepository variantRepository;
    private final AddressRepository addressRepository;
    private final ShipmentRepository shipmentRepository;
    private final UserRepository userRepository;

    /**
     * Get checkout summary (before placing order)
     */
    @Transactional(readOnly = true)
    public OrderSummary getCheckoutSummary(Long userId, String shippingMethod, String country) {

        // Get user's cart
        CartResponse cart = cartService.getCartForUser(userId);

        if (cart.getItems().isEmpty()) {
            throw new IllegalStateException("Cart is empty");
        }

        // Calculate subtotal
        BigDecimal subtotal = cart.getTotalPrice();

        // Calculate shipping
        ShippingRateResponse shippingRate = shippingService.calculateShippingCost(
                ShippingRateRequest.builder()
                        .destinationCountry(country)
                        .shippingMethod(shippingMethod)
                        .weight(BigDecimal.ONE) // Default weight, can be calculated from products
                        .build()
        );
        BigDecimal shippingCost = shippingRate.getCost();

        // Calculate tax
        TaxCalculationResponse taxResponse = taxService.calculateTax(
                new TaxCalculationRequest(subtotal, country, null)
        );
        BigDecimal taxAmount = taxResponse.getTaxAmount();

        // Calculate total
        BigDecimal total = subtotal.add(shippingCost).add(taxAmount);

        // Build summary
        List<OrderItemSummary> items = cart.getItems().stream()
                .map(item -> OrderItemSummary.builder()
                        .productName(item.getProductName())
                        .variantDetails(item.getVariantDetails())
                        .quantity(item.getQuantity())
                        .unitPrice(item.getPricePerUnit())
                        .subtotal(item.getSubtotal())
                        .build())
                .collect(Collectors.toList());

        return OrderSummary.builder()
                .items(items)
                .subtotal(subtotal)
                .shippingCost(shippingCost)
                .taxAmount(taxAmount)
                .discount(BigDecimal.ZERO)
                .total(total)
                .currency("USD")
                .build();
    }

    /**
     * Process complete checkout
     */
    public CheckoutResponse processCheckout(Long userId, CheckoutRequest request) {

        // 1. Validate cart
        CartResponse cart = cartService.getCartForUser(userId);
        if (cart.getItems().isEmpty()) {
            throw new IllegalStateException("Cart is empty");
        }

        // 2. Get and validate addresses
        AddressResponse shippingAddress = addressService.getAddressById(userId, request.getShippingAddressId());
        Address shippingAddr = addressRepository.findById(request.getShippingAddressId())
                .orElseThrow(() -> new ResourceNotFoundException("Shipping address", request.getShippingAddressId()));

        // 3. Calculate order summary
        OrderSummary summary = getCheckoutSummary(
                userId,
                request.getShippingMethod(),
                shippingAddress.getCountry()
        );

        // 4. Create order
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", userId));

        Order order = Order.builder()
                .user(user)
                .dateOrder(LocalDate.now())
                .status(OrderStatus.PENDING)
                .totalAmount(summary.getTotal())
                .build();
        order = orderRepository.save(order);

        // 5. Create order details and reduce stock
        for (CartItemResponse cartItem : cart.getItems()) {
            ProductVariant variant = variantRepository.findById(cartItem.getVariantId())
                    .orElseThrow(() -> new ResourceNotFoundException("Variant", cartItem.getVariantId()));

            // Validate stock
            if (variant.getStockQuantity() < cartItem.getQuantity()) {
                throw new InsufficientStockException(
                        cartItem.getProductName(),
                        cartItem.getQuantity(),
                        variant.getStockQuantity()
                );
            }

            // Create order detail
            OrderDetail detail = OrderDetail.builder()
                    .order(order)
                    .variant(variant)
                    .quantity(cartItem.getQuantity())
                    .price(cartItem.getPricePerUnit())
                    .build();
            orderDetailRepository.save(detail);

            // Reduce stock
            variant.setStockQuantity(variant.getStockQuantity() - cartItem.getQuantity());
            variantRepository.save(variant);
        }

        // 6. Create shipment
        Shipment shipment = Shipment.builder()
                .order(order)
                .shippingAddress(shippingAddr)
                .carrier(getCarrierForMethod(request.getShippingMethod()))
                .shippingMethod(ShippingMethod.valueOf(request.getShippingMethod().toUpperCase()))
                .status(OrderStatus.PENDING)
                .shippingCost(summary.getShippingCost())
                .estimatedDeliveryDate(calculateEstimatedDelivery(request.getShippingMethod()))
                .build();
        shipment = shipmentRepository.save(shipment);

        // 7. Process payment
        Payment payment;
        if ("STRIPE".equalsIgnoreCase(request.getPaymentMethod())) {
            payment = paymentService.confirmPayment(
                    order.getIdOrder(),
                    request.getPaymentIntentId(),
                    "STRIPE"
            );
        } else if ("COD".equalsIgnoreCase(request.getPaymentMethod())) {
            payment = paymentService.createCODPayment(order.getIdOrder());
        } else {
            throw new IllegalArgumentException("Unsupported payment method: " + request.getPaymentMethod());
        }

        // 8. Update order status if payment successful
        if (payment.isSuccessful()) {
            order.setStatus(OrderStatus.CONFIRMED);
            orderRepository.save(order);
        }

        // 9. Clear cart
        cartService.clearCart(userId);

        // 10. Build response
        return CheckoutResponse.builder()
                .orderId(order.getIdOrder())
                .orderNumber(generateOrderNumber(order.getIdOrder()))
                .orderSummary(summary)
                .paymentDetails(PaymentDetails.builder()
                        .paymentId(payment.getIdPayment())
                        .paymentMethod(payment.getPaymentMethod().name())
                        .status(payment.getStatus().name())
                        .amount(payment.getAmount())
                        .currency(payment.getCurrency())
                        .transactionId(payment.getTransactionId())
                        .build())
                .shippingDetails(ShippingDetails.builder()
                        .shipmentId(shipment.getIdShipment())
                        .carrier(shipment.getCarrier())
                        .method(shipment.getShippingMethod().name())
                        .trackingNumber(shipment.getTrackingNumber())
                        .estimatedDelivery(shipment.getEstimatedDeliveryDate())
                        .status(shipment.getStatus().name())
                        .shippingAddress(shippingAddress)
                        .build())
                .message("Order placed successfully!")
                .build();
    }

    // Helper methods
    private String getCarrierForMethod(String method) {
        switch (method.toUpperCase()) {
            case "STANDARD":
                return "Standard Post";
            case "EXPRESS":
                return "Express Courier";
            case "OVERNIGHT":
                return "Premium Express";
            default:
                return "Standard Carrier";
        }
    }

    private LocalDate calculateEstimatedDelivery(String method) {
        LocalDate today = LocalDate.now();
        switch (method.toUpperCase()) {
            case "OVERNIGHT":
                return today.plusDays(1);
            case "EXPRESS":
                return today.plusDays(3);
            case "STANDARD":
                return today.plusDays(7);
            default:
                return today.plusDays(7);
        }
    }

    private String generateOrderNumber(Long orderId) {
        return "ORD-" + String.format("%06d", orderId);
    }
}