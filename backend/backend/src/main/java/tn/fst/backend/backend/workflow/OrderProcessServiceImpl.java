package tn.fst.backend.backend.workflow;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import tn.fst.backend.backend.dto.OrderItemRequest;
import tn.fst.backend.backend.dto.OrderItemResponse;
import tn.fst.backend.backend.dto.OrderRequest;
import tn.fst.backend.backend.dto.OrderResponse;
import tn.fst.backend.backend.dto.OrderStatistics;
import tn.fst.backend.backend.entity.Client;
import tn.fst.backend.backend.entity.Invoice;
import tn.fst.backend.backend.entity.Order;
import tn.fst.backend.backend.entity.OrderDetail;
import tn.fst.backend.backend.entity.OrderStatus;
import tn.fst.backend.backend.entity.Product;
import tn.fst.backend.backend.entity.Stock;
import tn.fst.backend.backend.exeptions.InsufficientStockException;
import tn.fst.backend.backend.exeptions.ResourceNotFoundException;
import tn.fst.backend.backend.repository.ClientRepository;
import tn.fst.backend.backend.repository.InvoiceRepository;
import tn.fst.backend.backend.repository.OrderDetailRepository;
import tn.fst.backend.backend.repository.OrderRepository;
import tn.fst.backend.backend.repository.ProductRepository;
import tn.fst.backend.backend.repository.StockRepository;
import tn.fst.backend.backend.service.EmailService;


import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
@Transactional
public class OrderProcessServiceImpl implements OrderProcessService {

    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final StockRepository stockRepository;
    private final InvoiceRepository invoiceRepository;
    private final ClientRepository clientRepository;
    private final OrderDetailRepository orderDetailRepository;
    private final EmailService emailService;

    public OrderProcessServiceImpl(
            OrderRepository orderRepository,
            ProductRepository productRepository,
            StockRepository stockRepository,
            InvoiceRepository invoiceRepository,
            ClientRepository clientRepository,
            OrderDetailRepository orderDetailRepository,
            EmailService emailService) {
        this.orderRepository = orderRepository;
        this.productRepository = productRepository;
        this.stockRepository = stockRepository;
        this.invoiceRepository = invoiceRepository;
        this.clientRepository = clientRepository;
        this.orderDetailRepository = orderDetailRepository;
        this.emailService = emailService;
    }

    @Override
    public OrderResponse placeOrder(OrderRequest request) {

        if (request.getItems() == null || request.getItems().isEmpty()) {
            throw new IllegalArgumentException("Order must contain at least one item");
        }

        Client client = clientRepository.findById(request.getClientId())
                .orElseThrow(() -> new ResourceNotFoundException("Client", request.getClientId()));

        Order order = Order.builder()
                .client(client)
                .dateOrder(LocalDate.now())
                .status(OrderStatus.PENDING)
                .totalAmount(BigDecimal.ZERO)
                .build();

        order = orderRepository.save(order);

        BigDecimal total = BigDecimal.ZERO;
        List<OrderItemResponse> itemResponses = new ArrayList<>();

        for (OrderItemRequest item : request.getItems()) {

            Product product = productRepository.findById(item.getProductId())
                    .orElseThrow(() -> new ResourceNotFoundException("Product", item.getProductId()));

            Stock stock = stockRepository.findByProduct(product)
                    .orElseThrow(() -> new IllegalStateException(
                            "Stock not found for product: " + product.getName()));

            if (stock.getQuantity() < item.getQuantity()) {
                throw new InsufficientStockException(
                        product.getName(),
                        item.getQuantity(),
                        stock.getQuantity()
                );
            }

            stock.setQuantity(stock.getQuantity() - item.getQuantity());
            stockRepository.save(stock);

            BigDecimal unitPrice = product.getPrice();

            OrderDetail detail = OrderDetail.builder()
                    .order(order)
                    .product(product)
                    .quantity(item.getQuantity())
                    .price(unitPrice)
                    .build();

            orderDetailRepository.save(detail);

            BigDecimal subtotal = unitPrice.multiply(BigDecimal.valueOf(item.getQuantity()));
            total = total.add(subtotal);

            itemResponses.add(OrderItemResponse.builder()
                    .productId(product.getIdProduct())
                    .productName(product.getName())
                    .quantity(item.getQuantity())
                    .unitPrice(unitPrice)
                    .subtotal(subtotal)
                    .build());
        }

        order.setTotalAmount(total);
        order = orderRepository.save(order);

        String invoiceNumber = generateInvoiceNumber();
        Invoice invoice = Invoice.builder()
                .order(order)
                .totalAmount(total)
                .invoiceNumber(invoiceNumber)
                .issueDate(LocalDate.now())
                .build();

        invoiceRepository.save(invoice);

        return OrderResponse.builder()
                .orderId(order.getIdOrder())
                .clientId(client.getIdClient())
                .clientName(client.getUser().getFullname())
                .orderDate(order.getDateOrder())
                .status(order.getStatus())
                .totalAmount(order.getTotalAmount())
                .invoiceNumber(invoiceNumber)
                .items(itemResponses)
                .build();
    }

    @Override
    public OrderResponse updateOrderStatus(Long orderId, OrderStatus newStatus) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order", orderId));

        order.setStatus(newStatus);
        order = orderRepository.save(order);

        // Send notification
        emailService.sendOrderStatusEmail(order);

        return mapToOrderResponse(order);
    }

    @Override
    public OrderResponse cancelOrder(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order", orderId));


        if (order.getStatus() == OrderStatus.DELIVERED ||
                order.getStatus() == OrderStatus.SHIPPED) {
            throw new IllegalStateException(
                    "Cannot cancel order with status: " + order.getStatus());
        }

        List<OrderDetail> details = orderDetailRepository.findByOrder(order);
        for (OrderDetail detail : details) {
            Stock stock = stockRepository.findByProduct(detail.getProduct())
                    .orElseThrow(() -> new IllegalStateException(
                            "Stock not found for product: " + detail.getProduct().getName()));

            stock.setQuantity(stock.getQuantity() + detail.getQuantity());
            stockRepository.save(stock);
        }

        // Update order status
        order.setStatus(OrderStatus.CANCELLED);
        order = orderRepository.save(order);

        // Send notification
        emailService.sendOrderStatusEmail(order);

        return mapToOrderResponse(order);
    }

    @Override
    @Transactional(readOnly = true)
    public OrderResponse getOrderById(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order", orderId));

        return mapToOrderResponse(order);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<OrderResponse> getAllOrders(Pageable pageable) {
        Page<Order> ordersPage = orderRepository.findAll(pageable);
        return ordersPage.map(this::mapToOrderResponse);
    }

    private OrderResponse mapToOrderResponse(Order order) {
        List<OrderDetail> details = orderDetailRepository.findByOrder(order);

        List<OrderItemResponse> items = details.stream()
                .map(detail -> OrderItemResponse.builder()
                        .productId(detail.getProduct().getIdProduct())
                        .productName(detail.getProduct().getName())
                        .quantity(detail.getQuantity())
                        .unitPrice(detail.getPrice())
                        .subtotal(detail.getPrice()
                                .multiply(BigDecimal.valueOf(detail.getQuantity())))
                        .build())
                .toList();

        Invoice invoice = invoiceRepository.findByOrder(order).orElse(null);

        return OrderResponse.builder()
                .orderId(order.getIdOrder())
                .clientId(order.getClient().getIdClient())
                .clientName(order.getClient().getUser().getFullname())
                .orderDate(order.getDateOrder())
                .status(order.getStatus())
                .totalAmount(order.getTotalAmount())
                .invoiceNumber(invoice != null ? invoice.getInvoiceNumber() : null)
                .items(items)
                .build();
    }

    private String generateInvoiceNumber() {
        return "INV-" + System.currentTimeMillis();
    }

    public OrderStatistics getOrderStatistics() {
        List<Order> allOrders = orderRepository.findAll();

        long total = allOrders.size();
        BigDecimal revenue = allOrders.stream()
                .filter(o -> o.getStatus() == OrderStatus.DELIVERED)
                .map(Order::getTotalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        long pending = allOrders.stream()
                .filter(o -> o.getStatus() == OrderStatus.PENDING)
                .count();

        long completed = allOrders.stream()
                .filter(o -> o.getStatus() == OrderStatus.DELIVERED)
                .count();

        long cancelled = allOrders.stream()
                .filter(o -> o.getStatus() == OrderStatus.CANCELLED)
                .count();

        return OrderStatistics.builder()
                .totalOrders(total)
                .totalRevenue(revenue)
                .pendingOrders(pending)
                .completedOrders(completed)
                .cancelledOrders(cancelled)
                .build();
    }
}
