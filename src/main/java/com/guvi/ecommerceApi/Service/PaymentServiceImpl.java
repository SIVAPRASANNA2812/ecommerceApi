package com.guvi.ecommerceApi.Service;

import com.guvi.ecommerceApi.DTO.PaymentResponseDTO;
import com.guvi.ecommerceApi.Entity.OrderStatus;
import com.guvi.ecommerceApi.Exception.InvalidOrderStateException;
import com.guvi.ecommerceApi.Exception.ResourceNotFoundException;
import com.guvi.ecommerceApi.Model.Order;
import com.guvi.ecommerceApi.Repository.OrderRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class PaymentServiceImpl implements PaymentService {

    private final OrderRepository orderRepo;

    public PaymentServiceImpl(OrderRepository orderRepo) {
        this.orderRepo = orderRepo;
    }

    @Override
    public PaymentResponseDTO processPayment(String orderId) {
        Order order = orderRepo.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with id: " + orderId));

        // 1. Idempotency Check: Safe retry if order is already confirmed
        if (order.getStatus() == OrderStatus.CONFIRMED) {
            log.info("Idempotent payment call: Order {} is already CONFIRMED", orderId);
            return new PaymentResponseDTO(orderId, true, "Order is already paid and confirmed");
        }

        // 2. Allow payment ONLY for PLACED or previously PAYMENT_FAILED orders
        if (order.getStatus() != OrderStatus.PLACED && order.getStatus() != OrderStatus.PAYMENT_FAILED) {
            throw new InvalidOrderStateException("Payment cannot be processed for order in status: " + order.getStatus());
        }

        // 3. Simulate payment: 70% success rate
        boolean success = Math.random() < 0.7;

        if (success) {
            order.setStatus(OrderStatus.CONFIRMED);
            orderRepo.save(order);
            log.info("Payment SUCCESS for orderId: {}", orderId);
            return new PaymentResponseDTO(orderId, true, "Payment successful, order confirmed");
        } else {
            order.setStatus(OrderStatus.PAYMENT_FAILED);
            orderRepo.save(order);
            log.warn("Payment FAILED for orderId: {}", orderId);
            return new PaymentResponseDTO(orderId, false, "Payment failed, please retry");
        }
    }
}