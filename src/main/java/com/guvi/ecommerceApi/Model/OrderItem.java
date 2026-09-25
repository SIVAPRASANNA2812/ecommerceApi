package com.guvi.ecommerceApi.Model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OrderItem {

    private String productId;
    private int quantity;
    private BigDecimal priceAtPurchase;

}
