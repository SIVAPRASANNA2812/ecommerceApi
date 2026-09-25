package com.guvi.ecommerceApi.Service;

import com.guvi.ecommerceApi.DTO.PaymentResponseDTO;

public interface PaymentService {
    PaymentResponseDTO processPayment(String orderId);
}
