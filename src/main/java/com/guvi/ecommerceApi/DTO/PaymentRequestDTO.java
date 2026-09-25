package com.guvi.ecommerceApi.DTO;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class PaymentRequestDTO {
    @NotBlank
    private String orderId;
}
