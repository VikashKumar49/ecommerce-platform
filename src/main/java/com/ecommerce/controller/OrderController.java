package com.ecommerce.controller;

import com.ecommerce.dto.CreateOrderRequest;
import com.ecommerce.dto.OrderDTO;
import com.ecommerce.entity.Order;
import com.ecommerce.service.OrderService;
import com.ecommerce.util.JwtUtil;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/orders")
@CrossOrigin(origins = "*", maxAge = 3600)
public class OrderController {

    @Autowired
    private OrderService orderService;

    @Autowired
    private JwtUtil jwtUtil;

    /**
     * Create new order
     * POST /api/orders
     */
    @PostMapping
    public ResponseEntity<?> createOrder(
            @RequestHeader("Authorization") String token,
            @Valid @RequestBody CreateOrderRequest request) {
        try {
            String jwt = token.substring(7);
            if (!jwtUtil.validateToken(jwt)) {
                throw new IllegalArgumentException("Invalid token");
            }

            Long userId = extractUserIdFromToken(jwt);
            OrderDTO order = orderService.createOrder(userId, request.getShippingAddress(),
                    request.getTaxRate(), request.getShippingCost());

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Order created successfully");
            response.put("order", order);

            return new ResponseEntity<>(response, HttpStatus.CREATED);
        } catch (IllegalArgumentException e) {
            Map<String, Object> response = new HashMap<>();
            response.put("error", e.getMessage());
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("error", e.getMessage());
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Get user's orders
     * GET /api/orders?page=0&size=10
     */
    @GetMapping
    public ResponseEntity<?> getUserOrders(
            @RequestHeader("Authorization") String token,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        try {
            String jwt = token.substring(7);
            if (!jwtUtil.validateToken(jwt)) {
                throw new IllegalArgumentException("Invalid token");
            }

            Long userId = extractUserIdFromToken(jwt);
            Pageable pageable = PageRequest.of(page, size);
            Page<OrderDTO> orders = orderService.getUserOrders(userId, pageable);

            return new ResponseEntity<>(orders, HttpStatus.OK);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("error", e.getMessage());
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Get all orders (Admin only)
     * GET /api/orders/admin/all?page=0&size=10
     */
    @GetMapping("/admin/all")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> getAllOrders(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        try {
            Pageable pageable = PageRequest.of(page, size);
            Page<OrderDTO> orders = orderService.getAllOrders(pageable);
            return new ResponseEntity<>(orders, HttpStatus.OK);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("error", e.getMessage());
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Get order by ID
     * GET /api/orders/1
     */
    @GetMapping("/{id}")
    public ResponseEntity<?> getOrderById(@PathVariable Long id) {
        try {
            OrderDTO order = orderService.getOrderById(id);
            return new ResponseEntity<>(order, HttpStatus.OK);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("error", e.getMessage());
            return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
        }
    }

    /**
     * Update order status (Admin only)
     * PUT /api/orders/1/status?status=SHIPPED
     */
    @PutMapping("/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> updateOrderStatus(
            @PathVariable Long id,
            @RequestParam Order.OrderStatus status) {
        try {
            OrderDTO order = orderService.updateOrderStatus(id, status);

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Order status updated successfully");
            response.put("order", order);

            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("error", e.getMessage());
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Cancel order
     * PUT /api/orders/1/cancel
     */
    @PutMapping("/{id}/cancel")
    public ResponseEntity<?> cancelOrder(
            @RequestHeader("Authorization") String token,
            @PathVariable Long id) {
        try {
            String jwt = token.substring(7);
            if (!jwtUtil.validateToken(jwt)) {
                throw new IllegalArgumentException("Invalid token");
            }

            OrderDTO order = orderService.cancelOrder(id);

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Order cancelled successfully");
            response.put("order", order);

            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (IllegalArgumentException e) {
            Map<String, Object> response = new HashMap<>();
            response.put("error", e.getMessage());
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("error", e.getMessage());
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Helper method to extract user ID from token
     */
    private Long extractUserIdFromToken(String token) {
        return 1L; // Placeholder - implement properly in production
    }
}
