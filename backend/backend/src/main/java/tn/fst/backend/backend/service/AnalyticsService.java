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
        private final ClientRepository clientRepository;
        private final CategoryRepository categoryRepository;

        public DailySalesReport getDailySalesReport(LocalDate date) {
                return generateSalesReport(date, date, "DAILY");
        }

        public MonthlySalesReport getMonthlySalesReport(int year, int month) {
                LocalDate startOfMonth = LocalDate.of(year, month, 1);
                LocalDate endOfMonth = startOfMonth.with(TemporalAdjusters.lastDayOfMonth());

                // Bulk fetch for the whole month (all non-cancelled orders)
                List<Order> allOrders = orderRepository.findAllWithDetailsBetween(startOfMonth, endOfMonth);
                List<Payment> allPayments = paymentRepository.findByOrderIn(allOrders);
                Map<Long, Payment> paymentMap = allPayments.stream()
                                .collect(Collectors.toMap(p -> p.getOrder().getIdOrder(), p -> p, (p1, p2) -> p1));

                DailySalesReport monthSummary = generateSalesReportForData(allOrders, paymentMap, startOfMonth, endOfMonth, "MONTHLY");

                // Group by date in memory
                Map<LocalDate, List<Order>> ordersByDate = allOrders.stream()
                                .collect(Collectors.groupingBy(Order::getDateOrder));

                List<DailySalesBreakdown> dailyBreakdown = new ArrayList<>();
                for (LocalDate day = startOfMonth; !day.isAfter(endOfMonth); day = day.plusDays(1)) {
                        List<Order> dayOrders = ordersByDate.getOrDefault(day, Collections.emptyList());
                        long count = dayOrders.size();
                        BigDecimal revenue = dayOrders.stream()
                                        .map(Order::getTotalAmount)
                                        .reduce(BigDecimal.ZERO, BigDecimal::add);

                        dailyBreakdown.add(DailySalesBreakdown.builder()
                                        .date(day)
                                        .totalOrders(count)
                                        .totalRevenue(revenue)
                                        .build());
                }

                return MonthlySalesReport.builder()
                                .year(year)
                                .month(month)
                                .totalOrders(monthSummary.getTotalOrders())
                                .totalRevenue(monthSummary.getTotalRevenue())
                                .totalProfit(monthSummary.getTotalProfit())
                                .averageOrderValue(monthSummary.getAverageOrderValue())
                                .topProducts(monthSummary.getTopProducts())
                                .salesByPaymentMethod(monthSummary.getSalesByPaymentMethod())
                                .dailyBreakdown(dailyBreakdown)
                                .currency("TND")
                                .build();
        }

        public YearlySalesReport getYearlySalesReport(int year) {
                LocalDate startOfYear = LocalDate.of(year, 1, 1);
                LocalDate endOfYear = LocalDate.of(year, 12, 31);

                List<Order> allOrders = orderRepository.findAllWithDetailsBetween(startOfYear, endOfYear);
                List<Payment> allPayments = paymentRepository.findByOrderIn(allOrders);
                Map<Long, Payment> paymentMap = allPayments.stream()
                                .collect(Collectors.toMap(p -> p.getOrder().getIdOrder(), p -> p, (p1, p2) -> p1));

                DailySalesReport yearSummary = generateSalesReportForData(allOrders, paymentMap, startOfYear, endOfYear, "YEARLY");

                Map<Integer, List<Order>> ordersByMonth = allOrders.stream()
                                .collect(Collectors.groupingBy(o -> o.getDateOrder().getMonthValue()));

                List<MonthlySalesBreakdown> monthlyBreakdown = new ArrayList<>();
                for (int month = 1; month <= 12; month++) {
                        List<Order> monthOrders = ordersByMonth.getOrDefault(month, Collections.emptyList());
                        long count = monthOrders.size();
                        BigDecimal revenue = monthOrders.stream()
                                        .map(Order::getTotalAmount)
                                        .reduce(BigDecimal.ZERO, BigDecimal::add);

                        monthlyBreakdown.add(MonthlySalesBreakdown.builder()
                                        .month(month)
                                        .monthName(java.time.Month.of(month).name())
                                        .totalOrders(count)
                                        .totalRevenue(revenue)
                                        .build());
                }

                return YearlySalesReport.builder()
                                .year(year)
                                .totalOrders(yearSummary.getTotalOrders())
                                .totalRevenue(yearSummary.getTotalRevenue())
                                .totalProfit(yearSummary.getTotalProfit())
                                .averageOrderValue(yearSummary.getAverageOrderValue())
                                .topProducts(yearSummary.getTopProducts())
                                .salesByPaymentMethod(yearSummary.getSalesByPaymentMethod())
                                .monthlyBreakdown(monthlyBreakdown)
                                .currency("TND")
                                .build();
        }

        private DailySalesReport generateSalesReport(LocalDate startDate, LocalDate endDate, String period) {
                // Fetch ALL orders in the range to show "reel data"
                List<Order> orders = orderRepository.findAllWithDetailsBetween(startDate, endDate);
                List<Payment> payments = paymentRepository.findByOrderIn(orders);
                Map<Long, Payment> paymentMap = payments.stream()
                                .collect(Collectors.toMap(p -> p.getOrder().getIdOrder(), p -> p, (p1, p2) -> p1));

                return generateSalesReportForData(orders, paymentMap, startDate, endDate, period);
        }

        private DailySalesReport generateSalesReportForData(List<Order> orders, Map<Long, Payment> paymentMap, LocalDate startDate, LocalDate endDate, String period) {
                // Only count non-cancelled orders for main stats
                List<Order> activeOrders = orders.stream()
                                .filter(o -> o.getStatus() != OrderStatus.CANCELLED)
                                .collect(Collectors.toList());

                long totalOrders = activeOrders.size();
                BigDecimal totalRevenue = activeOrders.stream()
                                .map(Order::getTotalAmount)
                                .reduce(BigDecimal.ZERO, BigDecimal::add);

                BigDecimal averageOrderValue = totalOrders > 0
                                ? totalRevenue.divide(BigDecimal.valueOf(totalOrders), 2, RoundingMode.HALF_UP)
                                : BigDecimal.ZERO;

                Map<Long, ProductSalesData> productSales = new HashMap<>();
                for (Order order : orders) {
                        for (OrderDetail detail : order.getOrderDetails()) {
                                ProductVariant variant = detail.getVariant();
                                if (variant != null) {
                                        productSales.computeIfAbsent(variant.getIdVariant(), k -> new ProductSalesData())
                                                        .add(detail.getQuantity(), detail.getPrice()
                                                                        .multiply(BigDecimal.valueOf(detail.getQuantity())));
                                }
                        }
                }

                List<TopProductItem> topProducts = productSales.entrySet().stream()
                                .map(entry -> {
                                        // Product information is already fetched by JOIN FETCH
                                        // We can optimize this by finding the variant in the data we already have
                                        // but for simplicity we'll assume variant properties are accessible
                                        // Actually, let's just find any order detail that has this variant ID
                                        ProductVariant variant = orders.stream()
                                                        .flatMap(o -> o.getOrderDetails().stream())
                                                        .map(OrderDetail::getVariant)
                                                        .filter(v -> v.getIdVariant().equals(entry.getKey()))
                                                        .findFirst()
                                                        .orElse(null);

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

                Map<PaymentMethod, BigDecimal> methodRevenue = new HashMap<>();
                Map<PaymentMethod, Long> methodCounts = new HashMap<>();

                for (Order order : orders) {
                        Payment payment = paymentMap.get(order.getIdOrder());
                        if (payment != null) {
                                PaymentMethod method = payment.getPaymentMethod();
                                methodRevenue.merge(method, order.getTotalAmount(), BigDecimal::add);
                                methodCounts.merge(method, 1L, Long::sum);
                        }
                }

                List<PaymentMethodSales> salesByPaymentMethod = methodRevenue.entrySet().stream()
                                .map(e -> PaymentMethodSales.builder()
                                                .paymentMethod(e.getKey().name())
                                                .totalAmount(e.getValue())
                                                .orderCount(methodCounts.getOrDefault(e.getKey(), 0L))
                                                .build())
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
                List<Order> orders = orderRepository.findByDateOrderBetweenAndStatus(
                                startDate,
                                endDate,
                                OrderStatus.DELIVERED);

                Map<Long, ProductSalesData> productSales = new HashMap<>();
                for (Order order : orders) {
                        List<OrderDetail> details = orderDetailRepository.findByOrder(order);
                        for (OrderDetail detail : details) {
                                Long productId = detail.getVariant().getProduct().getIdProduct();
                                productSales.computeIfAbsent(productId, k -> new ProductSalesData())
                                                .add(detail.getQuantity(), detail.getPrice()
                                                                .multiply(BigDecimal.valueOf(detail.getQuantity())));
                        }
                }

                List<TopProductDetail> topProducts = productSales.entrySet().stream()
                                .map(entry -> {
                                        Product product = productRepository.findById(entry.getKey()).orElse(null);
                                        if (product == null)
                                                return null;
                                        return TopProductDetail.builder()
                                                        .productId(product.getIdProduct())
                                                        .productName(product.getName())
                                                        .category(product.getCategory() != null
                                                                        ? product.getCategory().getName()
                                                                        : "")
                                                        .quantitySold(entry.getValue().getQuantity())
                                                        .revenue(entry.getValue().getRevenue())
                                                        .averagePrice(entry.getValue().getRevenue().divide(
                                                                        BigDecimal.valueOf(
                                                                                        entry.getValue().getQuantity()),
                                                                        2, RoundingMode.HALF_UP))
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

        public CustomerAnalyticsReport getCustomerAnalytics(LocalDate startDate, LocalDate endDate) {
                List<User> allCustomers = userRepository.findAll();
                long totalCustomers = allCustomers.size();

                List<Order> orders = orderRepository.findByDateOrderBetween(
                                startDate.atStartOfDay(),
                                endDate.atTime(23, 59, 59));

                Set<Long> activeCustomers = orders.stream()
                                .map(o -> o.getUser().getIdUser())
                                .collect(Collectors.toSet());

                long totalActiveCustomers = activeCustomers.size();

                long newCustomers = allCustomers.stream()
                                .filter(u -> u.getCreatedAt().isAfter(startDate.minusDays(1)) &&
                                                u.getCreatedAt().isBefore(endDate.plusDays(1)))
                                .count();

                Map<Long, CustomerValue> customerValues = new HashMap<>();
                for (Order order : orders) {
                        Long userId = order.getUser().getIdUser();
                        customerValues.computeIfAbsent(userId, k -> new CustomerValue())
                                        .add(order.getTotalAmount());
                }

                BigDecimal avgCustomerValue = customerValues.isEmpty() ? BigDecimal.ZERO
                                : customerValues.values().stream()
                                                .map(CustomerValue::getTotalSpent)
                                                .reduce(BigDecimal.ZERO, BigDecimal::add)
                                                .divide(BigDecimal.valueOf(customerValues.size()), 2,
                                                                RoundingMode.HALF_UP);

                List<TopCustomer> topCustomers = customerValues.entrySet().stream()
                                .map(entry -> {
                                        User user = userRepository.findById(entry.getKey()).orElse(null);
                                        if (user == null)
                                                return null;
                                        return TopCustomer.builder()
                                                        .userId(user.getIdUser())
                                                        .customerName(user.getUsername())
                                                        .email(user.getEmail())
                                                        .totalOrders(orders.stream()
                                                                        .filter(o -> o.getUser().getIdUser()
                                                                                        .equals(entry.getKey()))
                                                                        .count())
                                                        .totalSpent(entry.getValue().getTotalSpent())
                                                        .build();
                                })
                                .filter(Objects::nonNull)
                                .sorted((a, b) -> b.getTotalSpent().compareTo(a.getTotalSpent()))
                                .limit(10)
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

        public DashboardData getDashboardData() {
                LocalDate today = LocalDate.now();
                LocalDate startOfMonth = today.with(TemporalAdjusters.firstDayOfMonth());
                LocalDate thirtyDaysAgo = today.minusDays(30);

                DailySalesReport todayStats = getDailySalesReport(today);
                MonthlySalesReport monthStats = getMonthlySalesReport(today.getYear(), today.getMonthValue());
                YearlySalesReport yearStats = getYearlySalesReport(today.getYear());
                
                // Get 30-day trend for chart
                DailySalesReport trendStats = generateSalesReport(thirtyDaysAgo, today, "TREND");
                
                // Fetch grouping for trendStats in memory
                List<Order> trendOrders = orderRepository.findAllWithDetailsBetween(thirtyDaysAgo, today);
                Map<LocalDate, List<Order>> ordersByDate = trendOrders.stream()
                                .filter(o -> o.getStatus() != OrderStatus.CANCELLED)
                                .collect(Collectors.groupingBy(Order::getDateOrder));

                List<DailySalesBreakdown> thirtyDayBreakdown = new ArrayList<>();
                for (LocalDate day = thirtyDaysAgo; !day.isAfter(today); day = day.plusDays(1)) {
                        List<Order> dayOrders = ordersByDate.getOrDefault(day, Collections.emptyList());
                        thirtyDayBreakdown.add(DailySalesBreakdown.builder()
                                        .date(day)
                                        .totalOrders((long) dayOrders.size())
                                        .totalRevenue(dayOrders.stream().map(Order::getTotalAmount).reduce(BigDecimal.ZERO, BigDecimal::add))
                                        .build());
                }

                // Calc Stock Metrics
                long outOfStock = variantRepository.findAll().stream().filter(v -> v.getStockQuantity() <= 0).count();
                long lowStock = variantRepository.findAll().stream().filter(v -> v.getStockQuantity() > 0 && v.getStockQuantity() <= 10).count();

                // Calc Status Distribution
                Map<String, Long> statusDistribution = new HashMap<>();
                for (OrderStatus status : OrderStatus.values()) {
                        statusDistribution.put(status.name(), orderRepository.countByStatus(status));
                }

                // Calc Category Distribution
                Map<String, CategorySalesData> categorySales = new HashMap<>();
                List<Order> allRecentOrders = orderRepository.findAllWithDetailsBetween(startOfMonth, today);
                for (Order order : allRecentOrders) {
                        for (OrderDetail detail : order.getOrderDetails()) {
                                Product product = detail.getVariant().getProduct();
                                String categoryName = (product.getCategory() != null) ? product.getCategory().getName() : "Uncategorized";
                                categorySales.computeIfAbsent(categoryName, k -> new CategorySalesData(k, 0L, BigDecimal.ZERO))
                                                .setOrderCount(categorySales.get(categoryName).getOrderCount() + 1);
                                categorySales.get(categoryName).setRevenue(categorySales.get(categoryName).getRevenue().add(detail.getPrice().multiply(BigDecimal.valueOf(detail.getQuantity()))));
                        }
                }

                return DashboardData.builder()
                                .todayOrders(todayStats.getTotalOrders())
                                .todayRevenue(todayStats.getTotalRevenue())
                                .monthOrders(monthStats.getTotalOrders())
                                .monthRevenue(monthStats.getTotalRevenue())
                                .yearOrders(yearStats.getTotalOrders())
                                .yearRevenue(yearStats.getTotalRevenue())
                                .totalProducts(productRepository.count())
                                .totalClients(clientRepository.count())
                                .totalCategories(categoryRepository.count())
                                .outOfStockProducts(outOfStock)
                                .lowStockProducts(lowStock)
                                .topProductsThisMonth(monthStats.getTopProducts())
                                .topCategories(new ArrayList<>(categorySales.values()))
                                .ordersByStatus(statusDistribution)
                                .recentSalesData(thirtyDayBreakdown)
                                .currency("TND")
                                .generatedAt(LocalDateTime.now())
                                .build();
}

        private static class ProductSalesData {
                private int quantity = 0;
                private BigDecimal revenue = BigDecimal.ZERO;

                void add(int qty, BigDecimal rev) {
                        this.quantity += qty;
                        this.revenue = this.revenue.add(rev);
                }

                int getQuantity() {
                        return quantity;
                }

                BigDecimal getRevenue() {
                        return revenue;
                }
        }

        private static class CustomerValue {
                private BigDecimal totalSpent = BigDecimal.ZERO;

                void add(BigDecimal amount) {
                        this.totalSpent = this.totalSpent.add(amount);
                }

                BigDecimal getTotalSpent() {
                        return totalSpent;
                }
        }
}