package com.guvi.ecommerceApi.Model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CartItems {

    private String productId;
    private String name;
    private int quantity;
    private BigDecimal price;;
}
