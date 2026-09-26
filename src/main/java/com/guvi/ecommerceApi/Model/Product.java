package com.guvi.ecommerceApi.Model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Document(collection="product_info")
public class Product {
    @Id
    private String id ;
    @Indexed(unique = true)
    private String productId;
    private String name;
    private String description;
    private BigDecimal price;
    private int stockQuantity;
    @Indexed
    private String category;
}
