package com.guvi.ecommerceApi.DTO;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class OrderItemDTO {
    private String productId;
    private int quantity;
    private BigDecimal priceAtPurchase;
}
