package com.guvi.ecommerceApi.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import java.util.List;

@Data
@AllArgsConstructor
public class CartResponseDTO {
    private String userId;
    private List<CartItemResponseDTO> items;
}
