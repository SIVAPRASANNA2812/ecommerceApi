package com.guvi.ecommerceApi.Service;


import com.guvi.ecommerceApi.DTO.OrderResponseDTO;

import java.util.List;

public interface OrderService {

    OrderResponseDTO placeOrder(String userId);
    OrderResponseDTO cancelOrder(String orderId);
    OrderResponseDTO viewOrder(String orderId);
    List<OrderResponseDTO> viewOrdersByUser(String userId);

}
