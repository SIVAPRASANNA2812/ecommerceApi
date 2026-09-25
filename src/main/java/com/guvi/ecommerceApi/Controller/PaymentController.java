package com.guvi.ecommerceApi.Controller;

import com.guvi.ecommerceApi.DTO.PaymentRequestDTO;
import com.guvi.ecommerceApi.DTO.PaymentResponseDTO;
import com.guvi.ecommerceApi.Service.PaymentService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/payments")
public class PaymentController {

    private final PaymentService paymentService;
    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @PostMapping("/pay")
    public PaymentResponseDTO pay(@Valid @RequestBody PaymentRequestDTO request) {
        return paymentService.processPayment(request.getOrderId());
    }
}
