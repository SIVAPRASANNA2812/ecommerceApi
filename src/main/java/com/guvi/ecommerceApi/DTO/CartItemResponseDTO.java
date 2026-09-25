package com.guvi.ecommerceApi.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
public class CartItemResponseDTO {
    private String productId;
    private String name;
    private int quantity;
    private BigDecimal price;
}
