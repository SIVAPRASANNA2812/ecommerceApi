package com.guvi.ecommerceApi.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class PaymentResponseDTO {
    private String orderId;
    private boolean success;
    private String message;
}
