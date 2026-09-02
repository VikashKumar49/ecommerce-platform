package com.ecommerce.service;

import com.ecommerce.dto.OrderDTO;
import com.ecommerce.dto.OrderItemDTO;
import com.ecommerce.entity.*;
import com.ecommerce.exception.ResourceNotFoundException;
import com.ecommerce.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class OrderService {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private OrderItemRepository orderItemRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CartRepository cartRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private EmailUtil emailUtil;

    /**
     * Create order from cart
     */
    public OrderDTO createOrder(Long userId, String shippingAddress, BigDecimal taxRate, BigDecimal shippingCost) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        List<Cart> cartItems = cartRepository.findByUser(user);
        if (cartItems.isEmpty()) {
            throw new IllegalArgumentException("Cart is empty");
        }

        // Calculate subtotal
        BigDecimal subtotal = cartItems.stream()
                .map(item -> {
                    BigDecimal price = item.getProduct().getDiscountPrice() != null
                            ? item.getProduct().getDiscountPrice()
                            : item.getProduct().getPrice();
                    return price.multiply(BigDecimal.valueOf(item.getQuantity()));
                })
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Calculate tax
        BigDecimal taxAmount = subtotal.multiply(taxRate);

        // Calculate total
        BigDecimal totalAmount = subtotal.add(taxAmount).add(shippingCost);

        // Create order
        Order order = new Order();
        order.setUser(user);
        order.setSubtotal(subtotal);
        order.setTaxAmount(taxAmount);
        order.setShippingCost(shippingCost);
        order.setTotalAmount(totalAmount);
        order.setShippingAddress(shippingAddress);
        order.setStatus(Order.OrderStatus.PENDING);

        Order savedOrder = orderRepository.save(order);

        // Create order items and decrease stock
        for (Cart cartItem : cartItems) {
            OrderItem orderItem = new OrderItem();
            orderItem.setOrder(savedOrder);
            orderItem.setProduct(cartItem.getProduct());
            orderItem.setQuantity(cartItem.getQuantity());
            orderItem.setPrice(cartItem.getProduct().getDiscountPrice() != null
                    ? cartItem.getProduct().getDiscountPrice()
                    : cartItem.getProduct().getPrice());

            orderItemRepository.save(orderItem);

            // Decrease product stock
            Product product = cartItem.getProduct();
            product.setStock(product.getStock() - cartItem.getQuantity());
            productRepository.save(product);
        }

        // Clear cart
        cartRepository.deleteByUserId(userId);

        // Send order confirmation email
        emailUtil.sendOrderConfirmationEmail(user.getEmail(), savedOrder.getId(), user.getFirstName());

        return convertToDTO(savedOrder);
    }

    /**
     * Get user orders
     */
    public Page<OrderDTO> getUserOrders(Long userId, Pageable pageable) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        Page<Order> orders = orderRepository.findByUser(user, pageable);
        return orders.map(this::convertToDTO);
    }

    /**
     * Get all orders (Admin only)
     */
    public Page<OrderDTO> getAllOrders(Pageable pageable) {
        Page<Order> orders = orderRepository.findAll(pageable);
        return orders.map(this::convertToDTO);
    }

    /**
     * Get order by ID
     */
    public OrderDTO getOrderById(Long id) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with id: " + id));
        return convertToDTO(order);
    }

    /**
     * Update order status
     */
    public OrderDTO updateOrderStatus(Long id, Order.OrderStatus status) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with id: " + id));
        order.setStatus(status);
        Order updatedOrder = orderRepository.save(order);
        return convertToDTO(updatedOrder);
    }

    /**
     * Cancel order
     */
    public OrderDTO cancelOrder(Long id) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with id: " + id));

        // Only allow cancellation if order is pending or processing
        if (order.getStatus() == Order.OrderStatus.SHIPPED || order.getStatus() == Order.OrderStatus.DELIVERED) {
            throw new IllegalArgumentException("Cannot cancel order with status: " + order.getStatus());
        }

        // Restore product stock
        List<OrderItem> orderItems = orderItemRepository.findByOrderId(id);
        for (OrderItem item : orderItems) {
            Product product = item.getProduct();
            product.setStock(product.getStock() + item.getQuantity());
            productRepository.save(product);
        }

        order.setStatus(Order.OrderStatus.CANCELLED);
        Order updatedOrder = orderRepository.save(order);
        return convertToDTO(updatedOrder);
    }

    /**
     * Convert Order entity to OrderDTO
     */
    private OrderDTO convertToDTO(Order order) {
        OrderDTO dto = new OrderDTO();
        dto.setId(order.getId());
        dto.setUserId(order.getUser().getId());
        dto.setTotalAmount(order.getTotalAmount());
        dto.setSubtotal(order.getSubtotal());
        dto.setTaxAmount(order.getTaxAmount());
        dto.setShippingCost(order.getShippingCost());
        dto.setDiscountAmount(order.getDiscountAmount());
        dto.setShippingAddress(order.getShippingAddress());
        dto.setStatus(order.getStatus().toString());
        dto.setCreatedAt(order.getCreatedAt());

        List<OrderItemDTO> items = order.getItems().stream()
                .map(item -> new OrderItemDTO(
                        item.getId(),
                        item.getProduct().getId(),
                        item.getProduct().getName(),
                        item.getQuantity(),
                        item.getPrice()
                ))
                .collect(Collectors.toList());
        dto.setItems(items);

        return dto;
    }
}
