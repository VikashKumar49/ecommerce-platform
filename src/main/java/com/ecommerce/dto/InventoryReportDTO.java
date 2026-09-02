package com.ecommerce.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class InventoryReportDTO {
    private Long productId;
    private String productName;
    private String categoryName;
    private Integer currentStock;
    private Integer lowStockThreshold;
    private Boolean lowStock;
    private Double rating;
}
