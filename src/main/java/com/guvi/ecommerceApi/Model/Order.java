package com.guvi.ecommerceApi.Model;

import com.guvi.ecommerceApi.Entity.OrderStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Document(collection="order_info")
public class Order {
    @Id
    private String orderId ;

    private String userId;

    private List<OrderItem> orderItems;

    private BigDecimal totalAmount;

    private OrderStatus status;
}
