package com.guvi.ecommerceApi.Service;

import com.guvi.ecommerceApi.DTO.PaymentResponseDTO;
import com.guvi.ecommerceApi.Entity.OrderStatus;
import com.guvi.ecommerceApi.Model.Order;
import com.guvi.ecommerceApi.Repository.OrderRepository;
import org.springframework.stereotype.Service;

@Service
public class PaymentServiceImpl implements PaymentService {

    private final OrderRepository orderRepo;

    public PaymentServiceImpl(OrderRepository orderRepo) {
        this.orderRepo = orderRepo;
    }

    @Override
    public PaymentResponseDTO processPayment(String orderId) {
        Order order = orderRepo.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        // Idempotency: if already confirmed, return success
        if (order.getStatus() == OrderStatus.CONFIRMED) {
            return new PaymentResponseDTO(orderId, true, "Payment already processed");
        }

        // Explicit state validation
        if (order.getStatus() != OrderStatus.PLACED) {
            throw new RuntimeException("Payment can only be made for PLACED orders");
        }

        // Simulate payment: random success/failure
        boolean success = Math.random() < 0.7;

        if (success) {
            order.setStatus(OrderStatus.CONFIRMED);
            orderRepo.save(order);
            return new PaymentResponseDTO(orderId, true, "Payment successful, order confirmed");
        } else {
            order.setStatus(OrderStatus.PAYMENT_FAILED);
            orderRepo.save(order);
            return new PaymentResponseDTO(orderId, false, "Payment failed, please retry");
        }
    }
}
