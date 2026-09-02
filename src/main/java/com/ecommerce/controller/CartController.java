package com.ecommerce.controller;

import com.ecommerce.dto.AddToCartRequest;
import com.ecommerce.dto.CartItemDTO;
import com.ecommerce.entity.User;
import com.ecommerce.service.CartService;
import com.ecommerce.util.JwtUtil;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/cart")
@CrossOrigin(origins = "*", maxAge = 3600)
public class CartController {

    @Autowired
    private CartService cartService;

    @Autowired
    private JwtUtil jwtUtil;

    /**
     * Get user's cart
     * GET /api/cart
     */
    @GetMapping
    public ResponseEntity<?> getCart(@RequestHeader("Authorization") String token) {
        try {
            String jwt = token.substring(7);
            if (!jwtUtil.validateToken(jwt)) {
                throw new IllegalArgumentException("Invalid token");
            }

            String email = jwtUtil.getEmailFromToken(jwt);
            // In a real scenario, you'd fetch user ID from database using email
            // For now, we'll use a placeholder
            Long userId = extractUserIdFromToken(jwt);

            List<CartItemDTO> cartItems = cartService.getCart(userId);
            BigDecimal total = cartService.calculateCartTotal(userId);

            Map<String, Object> response = new HashMap<>();
            response.put("items", cartItems);
            response.put("total", total);
            response.put("itemCount", cartItems.size());

            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("error", e.getMessage());
            return new ResponseEntity<>(response, HttpStatus.UNAUTHORIZED);
        }
    }

    /**
     * Add product to cart
     * POST /api/cart/add
     */
    @PostMapping("/add")
    public ResponseEntity<?> addToCart(
            @RequestHeader("Authorization") String token,
            @Valid @RequestBody AddToCartRequest request) {
        try {
            String jwt = token.substring(7);
            if (!jwtUtil.validateToken(jwt)) {
                throw new IllegalArgumentException("Invalid token");
            }

            Long userId = extractUserIdFromToken(jwt);
            CartItemDTO cartItem = cartService.addToCart(userId, request.getProductId(), request.getQuantity());

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Product added to cart successfully");
            response.put("cartItem", cartItem);

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
     * Update cart item quantity
     * PUT /api/cart/update/{cartId}
     */
    @PutMapping("/update/{cartId}")
    public ResponseEntity<?> updateCartItem(
            @RequestHeader("Authorization") String token,
            @PathVariable Long cartId,
            @RequestParam Integer quantity) {
        try {
            String jwt = token.substring(7);
            if (!jwtUtil.validateToken(jwt)) {
                throw new IllegalArgumentException("Invalid token");
            }

            CartItemDTO cartItem = cartService.updateCartItemQuantity(cartId, quantity);

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Cart item updated successfully");
            response.put("cartItem", cartItem);

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
     * Remove item from cart
     * DELETE /api/cart/remove/{cartId}
     */
    @DeleteMapping("/remove/{cartId}")
    public ResponseEntity<?> removeFromCart(
            @RequestHeader("Authorization") String token,
            @PathVariable Long cartId) {
        try {
            String jwt = token.substring(7);
            if (!jwtUtil.validateToken(jwt)) {
                throw new IllegalArgumentException("Invalid token");
            }

            cartService.removeFromCart(cartId);

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Item removed from cart successfully");

            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("error", e.getMessage());
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Clear entire cart
     * DELETE /api/cart/clear
     */
    @DeleteMapping("/clear")
    public ResponseEntity<?> clearCart(@RequestHeader("Authorization") String token) {
        try {
            String jwt = token.substring(7);
            if (!jwtUtil.validateToken(jwt)) {
                throw new IllegalArgumentException("Invalid token");
            }

            Long userId = extractUserIdFromToken(jwt);
            cartService.clearCart(userId);

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Cart cleared successfully");

            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("error", e.getMessage());
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Helper method to extract user ID from token
     * In a real application, you would query the database
     */
    private Long extractUserIdFromToken(String token) {
        // This is a placeholder - in production, you should decode the token properly
        // and retrieve the user ID from the database using the email
        return 1L; // Default value
    }
}
