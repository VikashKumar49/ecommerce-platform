package com.ecommerce.service;

import com.ecommerce.dto.CartItemDTO;
import com.ecommerce.entity.Cart;
import com.ecommerce.entity.Product;
import com.ecommerce.entity.User;
import com.ecommerce.exception.ResourceNotFoundException;
import com.ecommerce.repository.CartRepository;
import com.ecommerce.repository.ProductRepository;
import com.ecommerce.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@DisplayName("CartService Unit Tests")
class CartServiceTest {

    @Mock
    private CartRepository cartRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private CartService cartService;

    private User testUser;
    private Product testProduct;
    private Cart testCart;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        testUser = new User();
        testUser.setId(1L);
        testUser.setEmail("test@example.com");

        testProduct = new Product();
        testProduct.setId(1L);
        testProduct.setName("Laptop");
        testProduct.setPrice(new BigDecimal("999.99"));
        testProduct.setStock(10);

        testCart = new Cart();
        testCart.setId(1L);
        testCart.setUser(testUser);
        testCart.setProduct(testProduct);
        testCart.setQuantity(2);
    }

    @Test
    @DisplayName("Should add product to cart successfully")
    void testAddToCartSuccess() {
        // Arrange
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(testUser));
        when(productRepository.findById(anyLong())).thenReturn(Optional.of(testProduct));
        when(cartRepository.findByUserIdAndProductId(anyLong(), anyLong())).thenReturn(Optional.empty());
        when(cartRepository.save(any(Cart.class))).thenReturn(testCart);

        // Act
        CartItemDTO result = cartService.addToCart(1L, 1L, 2);

        // Assert
        assertNotNull(result);
        assertEquals(1L, result.getProductId());
        assertEquals(2, result.getQuantity());
        verify(cartRepository, times(1)).save(any(Cart.class));
    }

    @Test
    @DisplayName("Should throw exception when product not found")
    void testAddToCartProductNotFound() {
        // Arrange
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(testUser));
        when(productRepository.findById(anyLong())).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> {
            cartService.addToCart(1L, 999L, 2);
        });
    }

    @Test
    @DisplayName("Should throw exception when insufficient stock")
    void testAddToCartInsufficientStock() {
        // Arrange
        testProduct.setStock(1);
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(testUser));
        when(productRepository.findById(anyLong())).thenReturn(Optional.of(testProduct));

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            cartService.addToCart(1L, 1L, 5); // More than available stock
        });
    }

    @Test
    @DisplayName("Should update cart item quantity successfully")
    void testUpdateCartItemQuantitySuccess() {
        // Arrange
        when(cartRepository.findById(anyLong())).thenReturn(Optional.of(testCart));
        when(cartRepository.save(any(Cart.class))).thenReturn(testCart);

        // Act
        CartItemDTO result = cartService.updateCartItemQuantity(1L, 5);

        // Assert
        assertNotNull(result);
        verify(cartRepository, times(1)).save(any(Cart.class));
    }

    @Test
    @DisplayName("Should throw exception when quantity is invalid")
    void testUpdateCartItemQuantityInvalid() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            cartService.updateCartItemQuantity(1L, 0);
        });
    }
}
