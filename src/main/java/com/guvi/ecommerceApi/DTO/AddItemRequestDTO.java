package com.guvi.ecommerceApi.DTO;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class AddItemRequestDTO {
    @NotBlank
    private String userId;

    @NotBlank
    private String productId;

    @Min(1)
    private int quantity;
}
