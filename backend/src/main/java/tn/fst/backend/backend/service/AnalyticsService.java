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
        List<Order> orders = orderRepository.findByDateOrderBetweenAndStatus(
                startDate.atStartOfDay(),
                endDate.atTime(23, 59, 59),
                OrderStatus.DELIVERED
        );

        long totalOrders = orders.size();

        BigDecimal totalRevenue = orders.stream()
                .map(Order::getTotalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal averageOrderValue = totalOrders > 0 ?
                totalRevenue.divide(BigDecimal.valueOf(totalOrders), 2, RoundingMode.HALF_UP) :
                BigDecimal.ZERO;

        Map<Long, ProductSalesData> productSales = new HashMap<>();
        for (Order order : orders) {
            List<OrderDetail> details = orderDetailRepository.findByOrder(order);
            for (OrderDetail detail : details) {
                Long variantId = detail.getVariant().getIdVariant();
                productSales.computeIfAbsent(variantId, k -> new ProductSalesData())
                        .add(detail.getQuantity(), detail.getPrice().multiply(BigDecimal.valueOf(detail.getQuantity())));
            }
        }

        List<TopProductItem> topProducts = productSales.entrySet().stream()
                .map(entry -> {
                    ProductVariant variant = variantRepository.findById(entry.getKey()).orElse(null);
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

        Map<String, BigDecimal> paymentMethodSales = new HashMap<>();
        for (Order order : orders) {
            Payment payment = paymentRepository.findByOrder(order).orElse(null);
            if (payment != null) {
                String method = payment.getPaymentMethod().name();
                paymentMethodSales.merge(method, order.getTotalAmount(), BigDecimal::add);
            }
        }

        List<PaymentMethodSales> salesByPaymentMethod = paymentMethodSales.entrySet().stream()
                .map(e -> PaymentMethodSales.builder()
                        .paymentMethod(e.getKey())
                        .totalAmount(e.getValue())
                        .orderCount(orders.stream()
                                .filter(o -> paymentRepository.findByOrder(o)
                                        .map(p -> p.getPaymentMethod().name().equals(e.getKey()))
                                        .orElse(false))
                                .count())
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


    /**
     * Top produits par période
     */
    public TopProductsReport getTopProducts(LocalDate startDate, LocalDate endDate, int limit) {
        List<Order> orders = orderRepository.findByDateOrderBetweenAndStatus(
                startDate.atStartOfDay(),
                endDate.atTime(23, 59, 59),
                OrderStatus.DELIVERED
        );

        Map<Long, ProductSalesData> productSales = new HashMap<>();
        for (Order order : orders) {
            List<OrderDetail> details = orderDetailRepository.findByOrder(order);
            for (OrderDetail detail : details) {
                Long productId = detail.getVariant().getProduct().getIdProduct();
                productSales.computeIfAbsent(productId, k -> new ProductSalesData())
                        .add(detail.getQuantity(), detail.getPrice().multiply(BigDecimal.valueOf(detail.getQuantity())));
            }
        }

        List<TopProductDetail> topProducts = productSales.entrySet().stream()
                .map(entry -> {
                    Product product = productRepository.findById(entry.getKey()).orElse(null);
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
        List<User> allCustomers = userRepository.findAll();
        long totalCustomers = allCustomers.size();

        List<Order> orders = orderRepository.findByDateOrderBetween(
                startDate.atStartOfDay(),
                endDate.atTime(23, 59, 59)
        );

        Set<Long> activeCustomers = orders.stream()
                .map(o -> o.getUser().getIdUser())
                .collect(Collectors.toSet());

        long totalActiveCustomers = activeCustomers.size();

        long newCustomers = allCustomers.stream()
                .filter(u -> u.getCreatedAt().toLocalDate().isAfter(startDate.minusDays(1)) &&
                        u.getCreatedAt().toLocalDate().isBefore(endDate.plusDays(1)))
                .count();

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

        List<TopCustomer> topCustomers = customerValues.entrySet().stream()
                .map(entry -> {
                    User user = userRepository.findById(entry.getKey()).orElse(null);
                    if (user == null) return null;
                    return TopCustomer.builder()
                            .userId(user.getIdUser())
                            .customerName(user.getUsername())
                            .email(user.getEmail())
                            .totalOrders(orders.stream()
                                    .filter(o -> o.getUser().getIdUser().equals(entry.getKey()))
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


    /**
     * Dashboard principal
     */
    public DashboardData getDashboardData() {
        LocalDate today = LocalDate.now();
        LocalDate startOfMonth = today.with(TemporalAdjusters.firstDayOfMonth());
        LocalDate startOfYear = LocalDate.of(today.getYear(), 1, 1);

        DailySalesReport todayStats = getDailySalesReport(today);

        MonthlySalesReport monthStats = getMonthlySalesReport(today.getYear(), today.getMonthValue());

        YearlySalesReport yearStats = getYearlySalesReport(today.getYear());

        return DashboardData.builder()
                .todayOrders(todayStats.getTotalOrders())
                .todayRevenue(todayStats.getTotalRevenue())
                .monthOrders(monthStats.getTotalOrders())
                .monthRevenue(monthStats.getTotalRevenue())
                .yearOrders(yearStats.getTotalOrders())
                .yearRevenue(yearStats.getTotalRevenue())
                .topProductsThisMonth(monthStats.getTopProducts())
                .recentSalesData(monthStats.getDailyBreakdown())
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