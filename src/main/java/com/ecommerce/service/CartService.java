package com.ecommerce.service;

import com.ecommerce.dto.CartItemDTO;
import com.ecommerce.entity.Cart;
import com.ecommerce.entity.Product;
import com.ecommerce.entity.User;
import com.ecommerce.exception.ResourceNotFoundException;
import com.ecommerce.repository.CartRepository;
import com.ecommerce.repository.ProductRepository;
import com.ecommerce.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
public class CartService {

    @Autowired
    private CartRepository cartRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProductRepository productRepository;

    /**
     * Add product to cart
     */
    public CartItemDTO addToCart(Long userId, Long productId, Integer quantity) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + productId));

        // Check stock
        if (product.getStock() < quantity) {
            throw new IllegalArgumentException("Insufficient stock for product: " + product.getName());
        }

        // Check if product already in cart
        Optional<Cart> existingCart = cartRepository.findByUserIdAndProductId(userId, productId);

        Cart cart;
        if (existingCart.isPresent()) {
            cart = existingCart.get();
            cart.setQuantity(cart.getQuantity() + quantity);
        } else {
            cart = new Cart();
            cart.setUser(user);
            cart.setProduct(product);
            cart.setQuantity(quantity);
        }

        Cart savedCart = cartRepository.save(cart);
        return convertToDTO(savedCart);
    }

    /**
     * Get cart items for user
     */
    public List<CartItemDTO> getCart(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        List<Cart> cartItems = cartRepository.findByUser(user);
        return cartItems.stream().map(this::convertToDTO).collect(Collectors.toList());
    }

    /**
     * Update cart item quantity
     */
    public CartItemDTO updateCartItemQuantity(Long cartId, Integer quantity) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be greater than 0");
        }

        Cart cart = cartRepository.findById(cartId)
                .orElseThrow(() -> new ResourceNotFoundException("Cart item not found with id: " + cartId));

        // Check stock
        if (cart.getProduct().getStock() < quantity) {
            throw new IllegalArgumentException("Insufficient stock for product: " + cart.getProduct().getName());
        }

        cart.setQuantity(quantity);
        Cart updatedCart = cartRepository.save(cart);
        return convertToDTO(updatedCart);
    }

    /**
     * Remove item from cart
     */
    public void removeFromCart(Long cartId) {
        Cart cart = cartRepository.findById(cartId)
                .orElseThrow(() -> new ResourceNotFoundException("Cart item not found with id: " + cartId));
        cartRepository.delete(cart);
    }

    /**
     * Clear entire cart for user
     */
    public void clearCart(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));
        cartRepository.deleteByUserId(userId);
    }

    /**
     * Calculate cart total
     */
    public BigDecimal calculateCartTotal(Long userId) {
        List<Cart> cartItems = cartRepository.findByUser(
                userRepository.findById(userId)
                        .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId))
        );

        return cartItems.stream()
                .map(item -> {
                    BigDecimal price = item.getProduct().getDiscountPrice() != null
                            ? item.getProduct().getDiscountPrice()
                            : item.getProduct().getPrice();
                    return price.multiply(BigDecimal.valueOf(item.getQuantity()));
                })
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /**
     * Convert Cart entity to CartItemDTO
     */
    private CartItemDTO convertToDTO(Cart cart) {
        CartItemDTO dto = new CartItemDTO();
        dto.setId(cart.getId());
        dto.setProductId(cart.getProduct().getId());
        dto.setProductName(cart.getProduct().getName());
        dto.setPrice(cart.getProduct().getPrice());
        dto.setDiscountPrice(cart.getProduct().getDiscountPrice());
        dto.setQuantity(cart.getQuantity());
        dto.setImageUrl(cart.getProduct().getImageUrl());
        dto.setStock(cart.getProduct().getStock());
        return dto;
    }
}
