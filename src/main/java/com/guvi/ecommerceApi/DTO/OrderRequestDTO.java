package com.guvi.ecommerceApi.DTO;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Valid
@Data
public class OrderRequestDTO {

    @NotBlank
    private String userId;
}
