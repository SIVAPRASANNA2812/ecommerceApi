package com.guvi.ecommerceApi.Controller;

import com.guvi.ecommerceApi.DTO.AddItemRequestDTO;
import com.guvi.ecommerceApi.DTO.CartItemResponseDTO;
import com.guvi.ecommerceApi.DTO.CartResponseDTO;
import com.guvi.ecommerceApi.DTO.UpdateQuantityRequestDTO;
import com.guvi.ecommerceApi.Model.Cart;
import com.guvi.ecommerceApi.Service.CartService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/cart")
public class CartController {

    private final CartService cartService;

    public CartController(CartService cartService) {
        this.cartService = cartService;
    }

    @GetMapping
    public CartResponseDTO getCart(@RequestParam String userId) {
        Cart cart = cartService.viewCart(userId);
        return mapToResponseDTO(cart);
    }

    @PostMapping("/items")
    public CartResponseDTO addItem(@RequestBody AddItemRequestDTO request) {
        Cart cart = cartService.addItem(request.getUserId(), request.getProductId(), request.getQuantity());
        return mapToResponseDTO(cart);
    }

    @PutMapping("/items/{productId}")
    public CartResponseDTO updateQuantity(@PathVariable String productId,
                                          @RequestBody UpdateQuantityRequestDTO request) {
        Cart cart = cartService.updateQuantity(request.getUserId(), productId, request.getQuantity());
        return mapToResponseDTO(cart);
    }

    @DeleteMapping("/items/{productId}")
    public CartResponseDTO removeItem(@PathVariable String productId,
                                      @RequestParam String userId) {
        Cart cart = cartService.removeItem(userId, productId);
        return mapToResponseDTO(cart);
    }

    private CartResponseDTO mapToResponseDTO(Cart cart) {
        List<CartItemResponseDTO> items = cart.getItems().stream()
                .map(i -> new CartItemResponseDTO(i.getProductId(), i.getName(), i.getQuantity(), i.getPrice()))
                .toList();
        return new CartResponseDTO(cart.getUserId(), items);
    }
}
