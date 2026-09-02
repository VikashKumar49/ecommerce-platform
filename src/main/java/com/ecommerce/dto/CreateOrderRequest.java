package com.ecommerce.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateOrderRequest {
    @NotBlank(message = "Shipping address is required")
    private String shippingAddress;

    @NotNull(message = "Tax rate is required")
    @Positive(message = "Tax rate must be positive")
    private BigDecimal taxRate;

    @NotNull(message = "Shipping cost is required")
    @Positive(message = "Shipping cost must be positive")
    private BigDecimal shippingCost;
}
