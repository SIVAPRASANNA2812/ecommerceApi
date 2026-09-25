package com.guvi.ecommerceApi.Controller;

import com.guvi.ecommerceApi.DTO.OrderRequestDTO;
import com.guvi.ecommerceApi.DTO.OrderResponseDTO;
import com.guvi.ecommerceApi.Service.OrderService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderService orderService;
    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping("/place")
    public OrderResponseDTO placeOrder(@Valid @RequestBody OrderRequestDTO request) {
        return orderService.placeOrder(request.getUserId());
    }

    @PutMapping("/cancel/{orderId}")
    public OrderResponseDTO cancelOrder(@PathVariable String orderId) {
        return orderService.cancelOrder(orderId);
    }

    @GetMapping("/{orderId}")
    public OrderResponseDTO viewOrder(@PathVariable String orderId) {
        return orderService.viewOrder(orderId);
    }

    @GetMapping("/user/{userId}")
    public List<OrderResponseDTO> viewOrdersByUser(@PathVariable String userId) {
        return orderService.viewOrdersByUser(userId);
    }
}
