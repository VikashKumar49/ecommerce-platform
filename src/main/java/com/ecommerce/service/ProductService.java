package com.ecommerce.service;

import com.ecommerce.dto.ProductDTO;
import com.ecommerce.entity.Category;
import com.ecommerce.entity.Product;
import com.ecommerce.exception.ResourceNotFoundException;
import com.ecommerce.repository.CategoryRepository;
import com.ecommerce.repository.ProductRepository;
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
public class ProductService {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    /**
     * Get all products with pagination
     */
    public Page<ProductDTO> getAllProducts(Pageable pageable) {
        Page<Product> products = productRepository.findByIsActiveTrue(pageable);
        return products.map(this::convertToDTO);
    }

    /**
     * Get available products (in stock)
     */
    public Page<ProductDTO> getAvailableProducts(Pageable pageable) {
        Page<Product> products = productRepository.findAvailableProducts(pageable);
        return products.map(this::convertToDTO);
    }

    /**
     * Get product by ID
     */
    public ProductDTO getProductById(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + id));
        return convertToDTO(product);
    }

    /**
     * Search products by name
     */
    public Page<ProductDTO> searchProducts(String query, Pageable pageable) {
        Page<Product> products = productRepository.findByNameContainingIgnoreCase(query, pageable);
        return products.map(this::convertToDTO);
    }

    /**
     * Get products by category
     */
    public Page<ProductDTO> getProductsByCategory(Long categoryId, Pageable pageable) {
        Page<Product> products = productRepository.findByCategoryId(categoryId, pageable);
        return products.map(this::convertToDTO);
    }

    /**
     * Create new product (Admin only)
     */
    public ProductDTO createProduct(String name, String description, BigDecimal price,
                                   BigDecimal discountPrice, Long categoryId, Integer stock, String imageUrl) {
        // Verify category exists
        Category category = null;
        if (categoryId != null) {
            category = categoryRepository.findById(categoryId)
                    .orElseThrow(() -> new ResourceNotFoundException("Category not found with id: " + categoryId));
        }

        Product product = new Product();
        product.setName(name);
        product.setDescription(description);
        product.setPrice(price);
        product.setDiscountPrice(discountPrice);
        product.setCategory(category);
        product.setStock(stock != null ? stock : 0);
        product.setImageUrl(imageUrl);
        product.setIsActive(true);
        product.setRating(0.0);
        product.setReviewCount(0);

        Product savedProduct = productRepository.save(product);
        return convertToDTO(savedProduct);
    }

    /**
     * Update product (Admin only)
     */
    public ProductDTO updateProduct(Long id, String name, String description, BigDecimal price,
                                   BigDecimal discountPrice, Long categoryId, Integer stock, String imageUrl) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + id));

        if (name != null && !name.isBlank()) {
            product.setName(name);
        }
        if (description != null && !description.isBlank()) {
            product.setDescription(description);
        }
        if (price != null) {
            product.setPrice(price);
        }
        if (discountPrice != null) {
            product.setDiscountPrice(discountPrice);
        }
        if (categoryId != null) {
            Category category = categoryRepository.findById(categoryId)
                    .orElseThrow(() -> new ResourceNotFoundException("Category not found with id: " + categoryId));
            product.setCategory(category);
        }
        if (stock != null) {
            product.setStock(stock);
        }
        if (imageUrl != null && !imageUrl.isBlank()) {
            product.setImageUrl(imageUrl);
        }

        Product updatedProduct = productRepository.save(product);
        return convertToDTO(updatedProduct);
    }

    /**
     * Delete product (Admin only)
     */
    public void deleteProduct(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + id));
        product.setIsActive(false);
        productRepository.save(product);
    }

    /**
     * Update product stock
     */
    public void updateStock(Long id, Integer quantity) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + id));
        product.setStock(quantity);
        productRepository.save(product);
    }

    /**
     * Decrease product stock
     */
    public void decreaseStock(Long id, Integer quantity) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + id));
        
        if (product.getStock() < quantity) {
            throw new IllegalArgumentException("Insufficient stock for product: " + id);
        }
        
        product.setStock(product.getStock() - quantity);
        productRepository.save(product);
    }

    /**
     * Update product rating
     */
    public void updateRating(Long id, Double rating, Integer reviewCount) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + id));
        product.setRating(rating);
        product.setReviewCount(reviewCount);
        productRepository.save(product);
    }

    /**
     * Convert Product entity to ProductDTO
     */
    private ProductDTO convertToDTO(Product product) {
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
