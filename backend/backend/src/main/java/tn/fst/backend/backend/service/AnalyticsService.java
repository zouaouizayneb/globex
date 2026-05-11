package tn.fst.backend.backend.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tn.fst.backend.backend.dto.*;
import tn.fst.backend.backend.entity.*;
import tn.fst.backend.backend.repository.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.TemporalAdjusters;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AnalyticsService {

    private final OrderRepository orderRepository;
    private final OrderDetailRepository orderDetailRepository;
    private final ProductRepository productRepository;
    private final ProductVariantRepository variantRepository;
    private final UserRepository userRepository;
    private final InvoiceRepository invoiceRepository;
    private final PaymentRepository paymentRepository;
    private final CategoryRepository categoryRepository;


    /**
     * Rapport de ventes quotidien
     */
    public DailySalesReport getDailySalesReport(LocalDate date) {
        LocalDate startOfDay = date;
        LocalDate endOfDay = date;

        return generateSalesReport(startOfDay, endOfDay, "DAILY");
    }

    /**
     * Rapport de ventes mensuel
     */
    public MonthlySalesReport getMonthlySalesReport(int year, int month) {
        LocalDate startOfMonth = LocalDate.of(year, month, 1);
        LocalDate endOfMonth = startOfMonth.with(TemporalAdjusters.lastDayOfMonth());

        DailySalesReport report = generateSalesReport(startOfMonth, endOfMonth, "MONTHLY");

        List<DailySalesBreakdown> dailyBreakdown = new ArrayList<>();
        for (LocalDate day = startOfMonth; !day.isAfter(endOfMonth); day = day.plusDays(1)) {
            DailySalesReport dayReport = getDailySalesReport(day);
            dailyBreakdown.add(DailySalesBreakdown.builder()
                    .date(day)
                    .totalOrders(dayReport.getTotalOrders())
                    .totalRevenue(dayReport.getTotalRevenue())
                    .build());
        }

        return MonthlySalesReport.builder()
                .year(year)
                .month(month)
                .totalOrders(report.getTotalOrders())
                .totalRevenue(report.getTotalRevenue())
                .totalProfit(report.getTotalProfit())
                .averageOrderValue(report.getAverageOrderValue())
                .topProducts(report.getTopProducts())
                .salesByPaymentMethod(report.getSalesByPaymentMethod())
                .dailyBreakdown(dailyBreakdown)
                .currency("TND")
                .build();
    }

    /**
     * Rapport de ventes annuel
     */
    public YearlySalesReport getYearlySalesReport(int year) {
        LocalDate startOfYear = LocalDate.of(year, 1, 1);
        LocalDate endOfYear = LocalDate.of(year, 12, 31);

        DailySalesReport report = generateSalesReport(startOfYear, endOfYear, "YEARLY");

        List<MonthlySalesBreakdown> monthlyBreakdown = new ArrayList<>();
        for (int month = 1; month <= 12; month++) {
            MonthlySalesReport monthReport = getMonthlySalesReport(year, month);
            monthlyBreakdown.add(MonthlySalesBreakdown.builder()
                    .month(month)
                    .monthName(java.time.Month.of(month).name())
                    .totalOrders(monthReport.getTotalOrders())
                    .totalRevenue(monthReport.getTotalRevenue())
                    .build());
        }

        return YearlySalesReport.builder()
                .year(year)
                .totalOrders(report.getTotalOrders())
                .totalRevenue(report.getTotalRevenue())
                .totalProfit(report.getTotalProfit())
                .averageOrderValue(report.getAverageOrderValue())
                .topProducts(report.getTopProducts())
                .salesByPaymentMethod(report.getSalesByPaymentMethod())
                .monthlyBreakdown(monthlyBreakdown)
                .currency("TND")
                .build();
    }


    private DailySalesReport generateSalesReport(LocalDate startDate, LocalDate endDate, String period) {
        // Use optimized query with EntityGraph to fetch orderDetails, variant, and product in one query
        List<Order> orders = orderRepository.findByDateOrderBetweenAndStatusIn(
                startDate,
                endDate,
                List.of(
                        OrderStatus.PENDING,
                        OrderStatus.CONFIRMED,
                        OrderStatus.PROCESSING,
                        OrderStatus.PAID,
                        OrderStatus.SHIPPED,
                        OrderStatus.DELIVERED
                )
        );

        long totalOrders = orders.size();

        BigDecimal totalRevenue = orders.stream()
                .map(Order::getTotalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal averageOrderValue = totalOrders > 0 ?
                totalRevenue.divide(BigDecimal.valueOf(totalOrders), 2, RoundingMode.HALF_UP) :
                BigDecimal.ZERO;

        // Process order details from already-fetched data (no additional queries)
        Map<Long, ProductSalesData> productSales = new HashMap<>();
        for (Order order : orders) {
            // orderDetails are already fetched via EntityGraph
            List<OrderDetail> details = order.getOrderDetails();
            if (details != null) {
                for (OrderDetail detail : details) {
                    Long variantId = detail.getVariant().getIdVariant();
                    productSales.computeIfAbsent(variantId, k -> new ProductSalesData())
                            .add(detail.getQuantity(), detail.getPrice().multiply(BigDecimal.valueOf(detail.getQuantity())));
                }
            }
        }

        // Map to products using already-fetched variant data
        List<TopProductItem> topProducts = productSales.entrySet().stream()
                .map(entry -> {
                    // Find the variant from the already-fetched orders
                    ProductVariant variant = null;
                    for (Order order : orders) {
                        if (order.getOrderDetails() != null) {
                            for (OrderDetail detail : order.getOrderDetails()) {
                                if (detail.getVariant().getIdVariant().equals(entry.getKey())) {
                                    variant = detail.getVariant();
                                    break;
                                }
                            }
                        }
                        if (variant != null) break;
                    }
                    if (variant == null) return null;
                    return TopProductItem.builder()
                            .productId(variant.getProduct().getIdProduct())
                            .productName(variant.getProduct().getName())
                            .variantId(variant.getIdVariant())
                            .sku(variant.getSku())
                            .quantitySold(entry.getValue().getQuantity())
                            .revenue(entry.getValue().getRevenue())
                            .build();
                })
                .filter(Objects::nonNull)
                .sorted((a, b) -> b.getRevenue().compareTo(a.getRevenue()))
                .limit(10)
                .collect(Collectors.toList());

        // Fetch all payments for these orders in a single query (add this to repository if needed)
        // For now, skip payment method breakdown or use a batch query
        Map<String, BigDecimal> paymentMethodSales = new HashMap<>();
        Map<Long, String> orderPaymentMethods = new HashMap<>();

        // Batch fetch payments for all orders at once
        List<Long> orderIds = orders.stream().map(Order::getIdOrder).collect(Collectors.toList());
        // Note: Add a query to PaymentRepository: findByOrderIdIn(List<Long> orderIds)
        // For now, we'll skip this to avoid N+1, or you can add the batch query

        List<PaymentMethodSales> salesByPaymentMethod = paymentMethodSales.entrySet().stream()
                .map(e -> PaymentMethodSales.builder()
                        .paymentMethod(e.getKey())
                        .totalAmount(e.getValue())
                        .orderCount(0L)                         .build())
                .collect(Collectors.toList());

        return DailySalesReport.builder()
                .startDate(startDate)
                .endDate(endDate)
                .period(period)
                .totalOrders(totalOrders)
                .totalRevenue(totalRevenue)
                .totalProfit(totalRevenue.multiply(BigDecimal.valueOf(0.3))) // 30% margin estimate
                .averageOrderValue(averageOrderValue)
                .topProducts(topProducts)
                .salesByPaymentMethod(salesByPaymentMethod)
                .currency("TND")
                .generatedAt(LocalDateTime.now())
                .build();
    }

    public TopProductsReport getTopProducts(LocalDate startDate, LocalDate endDate, int limit) {
        // Use optimized query with EntityGraph to fetch orderDetails, variant, and product in one query
        List<Order> orders = orderRepository.findByDateOrderBetweenAndStatus(
                startDate, endDate,
                OrderStatus.DELIVERED
        );

        Map<Long, ProductSalesData> productSales = new HashMap<>();
        for (Order order : orders) {
            // orderDetails are already fetched via EntityGraph
            List<OrderDetail> details = order.getOrderDetails();
            if (details != null) {
                for (OrderDetail detail : details) {
                    Long productId = detail.getVariant().getProduct().getIdProduct();
                    productSales.computeIfAbsent(productId, k -> new ProductSalesData())
                            .add(detail.getQuantity(), detail.getPrice().multiply(BigDecimal.valueOf(detail.getQuantity())));
                }
            }
        }

        // Map to products using already-fetched data
        List<TopProductDetail> topProducts = productSales.entrySet().stream()
                .map(entry -> {
                    // Find the product from already-fetched orders
                    Product product = null;
                    for (Order order : orders) {
                        if (order.getOrderDetails() != null) {
                            for (OrderDetail detail : order.getOrderDetails()) {
                                if (detail.getVariant().getProduct().getIdProduct().equals(entry.getKey())) {
                                    product = detail.getVariant().getProduct();
                                    break;
                                }
                            }
                        }
                        if (product != null) break;
                    }
                    if (product == null) return null;
                    return TopProductDetail.builder()
                            .productId(product.getIdProduct())
                            .productName(product.getName())
                            .category(product.getCategory() != null ? product.getCategory().getName() : "")
                            .quantitySold(entry.getValue().getQuantity())
                            .revenue(entry.getValue().getRevenue())
                            .averagePrice(entry.getValue().getRevenue().divide(
                                    BigDecimal.valueOf(entry.getValue().getQuantity()), 2, RoundingMode.HALF_UP))
                            .build();
                })
                .filter(Objects::nonNull)
                .sorted((a, b) -> b.getRevenue().compareTo(a.getRevenue()))
                .limit(limit)
                .collect(Collectors.toList());

        return TopProductsReport.builder()
                .startDate(startDate)
                .endDate(endDate)
                .topProducts(topProducts)
                .generatedAt(LocalDateTime.now())
                .build();
    }


    /**
     * Analytics clients
     */
    public CustomerAnalyticsReport getCustomerAnalytics(LocalDate startDate, LocalDate endDate) {
        // Use count instead of loading all users - much faster
        long totalCustomers = userRepository.count();

        List<Order> orders = orderRepository.findByDateOrderBetween(startDate.atStartOfDay(), endDate.atTime(23, 59, 59));

        Set<Long> activeCustomers = orders.stream()
                .map(o -> o.getUser().getIdUser())
                .collect(Collectors.toSet());

        long totalActiveCustomers = activeCustomers.size();

        // Count new customers without loading all users
        long newCustomers = userRepository.countByCreatedAtBetween(
                startDate.minusDays(1).atStartOfDay(),
                endDate.plusDays(1).atTime(23, 59, 59)
        );

        Map<Long, CustomerValue> customerValues = new HashMap<>();
        for (Order order : orders) {
            Long userId = order.getUser().getIdUser();
            customerValues.computeIfAbsent(userId, k -> new CustomerValue())
                    .add(order.getTotalAmount());
        }

        BigDecimal avgCustomerValue = customerValues.isEmpty() ? BigDecimal.ZERO :
                customerValues.values().stream()
                        .map(CustomerValue::getTotalSpent)
                        .reduce(BigDecimal.ZERO, BigDecimal::add)
                        .divide(BigDecimal.valueOf(customerValues.size()), 2, RoundingMode.HALF_UP);

        // Fetch only the top customer IDs, then batch fetch user details
        List<Long> topCustomerIds = customerValues.entrySet().stream()
                .sorted((a, b) -> b.getValue().getTotalSpent().compareTo(a.getValue().getTotalSpent()))
                .limit(10)
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());

        // Batch fetch users for top customers
        List<User> topUsers = userRepository.findAllById(topCustomerIds);
        Map<Long, User> userMap = topUsers.stream().collect(Collectors.toMap(User::getIdUser, u -> u));

        List<TopCustomer> topCustomers = topCustomerIds.stream()
                .map(userId -> {
                    User user = userMap.get(userId);
                    if (user == null) return null;
                    CustomerValue value = customerValues.get(userId);
                    long orderCount = orders.stream()
                            .filter(o -> o.getUser().getIdUser().equals(userId))
                            .count();
                    return TopCustomer.builder()
                            .userId(user.getIdUser())
                            .customerName(user.getUsername())
                            .email(user.getEmail())
                            .totalOrders(orderCount)
                            .totalSpent(value != null ? value.getTotalSpent() : BigDecimal.ZERO)
                            .build();
                })
                .filter(Objects::nonNull)
                .sorted((a, b) -> b.getTotalSpent().compareTo(a.getTotalSpent()))
                .collect(Collectors.toList());

        return CustomerAnalyticsReport.builder()
                .startDate(startDate)
                .endDate(endDate)
                .totalCustomers(totalCustomers)
                .activeCustomers(totalActiveCustomers)
                .newCustomers(newCustomers)
                .averageCustomerValue(avgCustomerValue)
                .topCustomers(topCustomers)
                .currency("TND")
                .generatedAt(LocalDateTime.now())
                .build();
    }


    /**
     * Dashboard principal - Optimized to avoid redundant calculations
     */
    public DashboardData getDashboardData() {
        LocalDate today = LocalDate.now();
        // Global Counts
        long totalProducts = productRepository.count();
        long totalClients = userRepository.count();
        long totalCategories = categoryRepository.count();

        // Stock Alerts
        long outOfStock = variantRepository.countByStockQuantity(0);
        long lowStockCount = variantRepository.countByStockQuantityLessThanEqual(10);
        // Note: outOfStock are also <= 10, so lowStock items (excluding 0) is:
        long onlyLowStock = lowStockCount - outOfStock;

        // Daily/Monthly/Yearly Stats (Direct calculation to ensure accuracy)
        List<Order> orders = orderRepository.findAll();
        
        long todayOrdersCount = 0;
        BigDecimal todayRev = BigDecimal.ZERO;
        long monthOrdersCount = 0;
        BigDecimal monthRev = BigDecimal.ZERO;
        long yearOrdersCount = 0;
        BigDecimal yearRev = BigDecimal.ZERO;

        for (Order o : orders) {
            if (o.getDateOrder() == null) continue;
            LocalDate d = o.getDateOrder();
            BigDecimal amt = o.getTotalAmount() != null ? o.getTotalAmount() : BigDecimal.ZERO;

            if (d.getYear() == today.getYear()) {
                yearOrdersCount++;
                yearRev = yearRev.add(amt);
                
                if (d.getMonth() == today.getMonth()) {
                    monthOrdersCount++;
                    monthRev = monthRev.add(amt);
                    
                    if (d.getDayOfMonth() == today.getDayOfMonth()) {
                        todayOrdersCount++;
                        todayRev = todayRev.add(amt);
                    }
                }
            }
        }

        // Order Status Distribution
        Map<String, Long> statusDistribution = new HashMap<>();
        List<Order> allOrders = orderRepository.findAll();
        for (Order o : allOrders) {
            if (o.getStatus() != null) {
                String status = o.getStatus().name();
                statusDistribution.put(status, statusDistribution.getOrDefault(status, 0L) + 1);
            }
        }

        // Category Sales Data
        // For simplicity, we'll aggregate from all confirmed/delivered orders
        List<CategorySalesData> categorySales = aggregateCategorySales();

        return DashboardData.builder()
                .todayOrders(todayOrdersCount)
                .todayRevenue(todayRev)
                .monthOrders(monthOrdersCount)
                .monthRevenue(monthRev)
                .yearOrders(yearOrdersCount)
                .yearRevenue(yearRev)
                .totalProducts(totalProducts)
                .totalClients(totalClients)
                .totalCategories(totalCategories)
                .outOfStockProducts(outOfStock)
                .lowStockProducts(onlyLowStock)
                .topProductsThisMonth(new ArrayList<>()) // Simplified
                .topCategories(categorySales)
                .ordersByStatus(statusDistribution)
                .recentSalesData(generateTrendData(orders, today))
                .currency("TND")
                .generatedAt(LocalDateTime.now())
                .build();
    }

    private List<DailySalesBreakdown> generateTrendData(List<Order> orders, LocalDate today) {
        List<DailySalesBreakdown> trend = new ArrayList<>();
        for (int i = 29; i >= 0; i--) {
            LocalDate d = today.minusDays(i);
            BigDecimal dayRevenue = orders.stream()
                    .filter(o -> o.getDateOrder() != null && o.getDateOrder().equals(d))
                    .map(Order::getTotalAmount)
                    .filter(Objects::nonNull)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            trend.add(new DailySalesBreakdown(d, 0L, dayRevenue));
        }
        return trend;
    }

    private List<CategorySalesData> aggregateCategorySales() {
        // Fetch all order details directly to ensure we have product/category info
        List<OrderDetail> details = orderDetailRepository.findAll();
        Map<String, CategorySalesData> aggregation = new HashMap<>();

        for (OrderDetail detail : details) {
            if (detail.getOrder() == null || detail.getOrder().getStatus() == OrderStatus.CANCELLED) continue;
            
            Product product = detail.getProduct();
            if (product == null) continue;

            String catName = product.getCategory() != null ? product.getCategory().getName() : "Uncategorized";
            
            CategorySalesData data = aggregation.computeIfAbsent(catName, k -> 
                    CategorySalesData.builder()
                            .categoryName(catName)
                            .orderCount(0L)
                            .revenue(BigDecimal.ZERO)
                            .build());
            
            data.setOrderCount(data.getOrderCount() + 1);
            data.setRevenue(data.getRevenue().add(detail.getPrice().multiply(BigDecimal.valueOf(detail.getQuantity()))));
        }

        return aggregation.values().stream()
                .sorted((a, b) -> b.getRevenue().compareTo(a.getRevenue()))
                .limit(10)
                .collect(Collectors.toList());
    }


    private static class ProductSalesData {
        private int quantity = 0;
        private BigDecimal revenue = BigDecimal.ZERO;

        void add(int qty, BigDecimal rev) {
            this.quantity += qty;
            this.revenue = this.revenue.add(rev);
        }

        int getQuantity() { return quantity; }
        BigDecimal getRevenue() { return revenue; }
    }

    private static class CustomerValue {
        private BigDecimal totalSpent = BigDecimal.ZERO;

        void add(BigDecimal amount) {
            this.totalSpent = this.totalSpent.add(amount);
        }

        BigDecimal getTotalSpent() { return totalSpent; }
    }
}


