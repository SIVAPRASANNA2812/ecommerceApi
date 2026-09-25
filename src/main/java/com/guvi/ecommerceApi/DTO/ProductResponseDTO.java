package com.guvi.ecommerceApi.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProductResponseDTO {

    private String id;
    private String productId;
    private String name;
    private BigDecimal price;
    private String category;
}
