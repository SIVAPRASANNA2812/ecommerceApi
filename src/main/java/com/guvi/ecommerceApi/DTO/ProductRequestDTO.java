package com.guvi.ecommerceApi.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProductRequestDTO {
    private String productId;
    private String name;
    private String description;
    private BigDecimal price;
    private int stockQuantity;
    private String category;
}
