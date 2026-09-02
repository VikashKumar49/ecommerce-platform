package com.ecommerce.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductDTO {
    private Long id;
    private String name;
    private String description;
    private BigDecimal price;
    private BigDecimal discountPrice;
    private Long categoryId;
    private String categoryName;
    private Integer stock;
    private Double rating;
    private Integer reviewCount;
    private String imageUrl;
    private Boolean isActive;
}
