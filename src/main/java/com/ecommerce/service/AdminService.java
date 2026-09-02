package com.ecommerce.service;

import com.ecommerce.dto.*;
import com.ecommerce.entity.*;
import com.ecommerce.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class AdminService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private PaymentRepository paymentRepository;

    /**
     * Get dashboard statistics
     */
    public DashboardStatsDTO getDashboardStats() {
        DashboardStatsDTO stats = new DashboardStatsDTO();

        // User stats
        stats.setTotalUsers(userRepository.count());

        // Product stats
        stats.setTotalProducts(productRepository.count());
        stats.setTotalCategories(categoryRepository.count());

        // Order stats
        List<Order> allOrders = orderRepository.findAll();
        stats.setTotalOrders((long) allOrders.size());

        // Revenue calculation
        BigDecimal totalRevenue = allOrders.stream()
                .filter(o -> o.getStatus() == Order.OrderStatus.DELIVERED || o.getStatus() == Order.OrderStatus.PROCESSING)
                .map(Order::getTotalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        stats.setTotalRevenue(totalRevenue.doubleValue());

        // Average order value
        if (!allOrders.isEmpty()) {
            BigDecimal avgOrderValue = totalRevenue.divide(BigDecimal.valueOf(allOrders.size()), 2, java.math.RoundingMode.HALF_UP);
            stats.setAverageOrderValue(avgOrderValue.doubleValue());
        }

        // Order status breakdown
        stats.setPendingOrders(countOrdersByStatus(allOrders, Order.OrderStatus.PENDING));
        stats.setProcessingOrders(countOrdersByStatus(allOrders, Order.OrderStatus.PROCESSING));
        stats.setShippedOrders(countOrdersByStatus(allOrders, Order.OrderStatus.SHIPPED));
        stats.setDeliveredOrders(countOrdersByStatus(allOrders, Order.OrderStatus.DELIVERED));
        stats.setCancelledOrders(countOrdersByStatus(allOrders, Order.OrderStatus.CANCELLED));

        return stats;
    }

    /**
     * Get sales report
     */
    public List<SalesReportDTO> getSalesReport() {
        List<Order> orders = orderRepository.findAll();
        return orders.stream()
                .map(order -> {
                    SalesReportDTO dto = new SalesReportDTO();
                    dto.setOrderId(order.getId());
                    dto.setCustomerName(order.getUser().getFirstName() + " " + order.getUser().getLastName());
                    dto.setCustomerEmail(order.getUser().getEmail());
                    dto.setTotalAmount(order.getTotalAmount());
                    dto.setOrderStatus(order.getStatus().toString());

                    // Get payment status
                    paymentRepository.findByOrderId(order.getId()).ifPresent(payment -> {
                        dto.setPaymentStatus(payment.getStatus().toString());
                    });

                    dto.setCreatedAt(order.getCreatedAt().toString());
                    return dto;
                })
                .collect(Collectors.toList());
    }

    /**
     * Get inventory report
     */
    public List<InventoryReportDTO> getInventoryReport() {
        List<Product> products = productRepository.findAll();
        return products.stream()
                .map(product -> {
                    InventoryReportDTO dto = new InventoryReportDTO();
                    dto.setProductId(product.getId());
                    dto.setProductName(product.getName());
                    dto.setCurrentStock(product.getStock());
                    dto.setLowStockThreshold(10); // Default low stock threshold
                    dto.setLowStock(product.getStock() < 10);
                    dto.setRating(product.getRating());

                    if (product.getCategory() != null) {
                        dto.setCategoryName(product.getCategory().getName());
                    }

                    return dto;
                })
                .collect(Collectors.toList());
    }

    /**
     * Get revenue by date (last 30 days)
     */
    public List<RevenueByDateDTO> getRevenueByDate(int days) {
        LocalDateTime startDate = LocalDateTime.now().minusDays(days);
        List<Order> orders = orderRepository.findAll().stream()
                .filter(o -> o.getCreatedAt().isAfter(startDate))
                .collect(Collectors.toList());

        return orders.stream()
                .collect(Collectors.groupingBy(
                        o -> o.getCreatedAt().toLocalDate().toString(),
                        Collectors.collectingAndThen(
                                Collectors.toList(),
                                list -> new RevenueByDateDTO(
                                        list.get(0).getCreatedAt().toLocalDate().toString(),
                                        list.stream().map(Order::getTotalAmount)
                                                .reduce(BigDecimal.ZERO, BigDecimal::add),
                                        (long) list.size()
                                )
                        )
                ))
                .values()
                .stream()
                .sorted(Comparator.comparing(RevenueByDateDTO::getDate))
                .collect(Collectors.toList());
    }

    /**
     * Get top selling products
     */
    public List<ProductDTO> getTopSellingProducts(int limit) {
        List<Order> orders = orderRepository.findAll();
        Map<Product, Integer> productSales = new HashMap<>();

        for (Order order : orders) {
            for (OrderItem item : order.getItems()) {
                productSales.put(item.getProduct(),
                        productSales.getOrDefault(item.getProduct(), 0) + item.getQuantity());
            }
        }

        return productSales.entrySet().stream()
                .sorted((a, b) -> b.getValue().compareTo(a.getValue()))
                .limit(limit)
                .map(entry -> convertProductToDTO(entry.getKey()))
                .collect(Collectors.toList());
    }

    /**
     * Get low stock products
     */
    public List<InventoryReportDTO> getLowStockProducts(int threshold) {
        return productRepository.findAll().stream()
                .filter(p -> p.getStock() <= threshold)
                .map(product -> {
                    InventoryReportDTO dto = new InventoryReportDTO();
                    dto.setProductId(product.getId());
                    dto.setProductName(product.getName());
                    dto.setCurrentStock(product.getStock());
                    dto.setLowStockThreshold(threshold);
                    dto.setLowStock(true);
                    dto.setRating(product.getRating());

                    if (product.getCategory() != null) {
                        dto.setCategoryName(product.getCategory().getName());
                    }

                    return dto;
                })
                .collect(Collectors.toList());
    }

    /**
     * Get customer statistics
     */
    public Map<String, Object> getCustomerStats() {
        List<User> users = userRepository.findAll();
        List<Order> orders = orderRepository.findAll();

        Map<String, Object> stats = new HashMap<>();
        stats.put("totalCustomers", users.size());
        stats.put("activeCustomers", users.stream().filter(User::getIsActive).count());
        stats.put("customersWithOrders", users.stream()
                .filter(u -> orders.stream().anyMatch(o -> o.getUser().getId().equals(u.getId())))
                .count());
        stats.put("emailVerifiedCustomers", users.stream().filter(User::getIsEmailVerified).count());

        return stats;
    }

    /**
     * Helper method to count orders by status
     */
    private Long countOrdersByStatus(List<Order> orders, Order.OrderStatus status) {
        return orders.stream()
                .filter(o -> o.getStatus() == status)
                .count();
    }

    /**
     * Convert Product to ProductDTO
     */
    private ProductDTO convertProductToDTO(Product product) {
        ProductDTO dto = new ProductDTO();
        dto.setId(product.getId());
        dto.setName(product.getName());
        dto.setDescription(product.getDescription());
        dto.setPrice(product.getPrice());
        dto.setDiscountPrice(product.getDiscountPrice());
        dto.setStock(product.getStock());
        dto.setRating(product.getRating());
        dto.setReviewCount(product.getReviewCount());
        dto.setImageUrl(product.getImageUrl());
        dto.setIsActive(product.getIsActive());

        if (product.getCategory() != null) {
            dto.setCategoryId(product.getCategory().getId());
            dto.setCategoryName(product.getCategory().getName());
        }

        return dto;
    }
}
