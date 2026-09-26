package com.guvi.ecommerceApi.Controller;

import com.guvi.ecommerceApi.DTO.OrderResponseDTO;
import com.guvi.ecommerceApi.Service.OrderService;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    // POST /api/orders/place (Places order for the authenticated user)
    @PostMapping("/place")
    public OrderResponseDTO placeOrder(Authentication authentication) {
        String username = authentication.getName();
        return orderService.placeOrder(username);
    }

    // PUT /api/orders/cancel/{orderId}
    @PutMapping("/cancel/{orderId}")
    public OrderResponseDTO cancelOrder(@PathVariable String orderId) {
        return orderService.cancelOrder(orderId);
    }

    // GET /api/orders/{orderId}
    @GetMapping("/{orderId}")
    public OrderResponseDTO viewOrder(@PathVariable String orderId) {
        return orderService.viewOrder(orderId);
    }

    // GET /api/orders/my-orders (Returns all orders for the currently logged-in user)
    @GetMapping("/my-orders")
    public List<OrderResponseDTO> viewMyOrders(Authentication authentication) {
        String username = authentication.getName();
        return orderService.viewOrdersByUser(username);
    }
}