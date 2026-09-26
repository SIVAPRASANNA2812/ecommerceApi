package com.guvi.ecommerceApi.Controller;

import com.guvi.ecommerceApi.DTO.AddItemRequestDTO;
import com.guvi.ecommerceApi.DTO.CartItemResponseDTO;
import com.guvi.ecommerceApi.DTO.CartResponseDTO;
import com.guvi.ecommerceApi.DTO.UpdateQuantityRequestDTO;
import com.guvi.ecommerceApi.Model.Cart;
import com.guvi.ecommerceApi.Service.CartService;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/cart")
public class CartController {

    private final CartService cartService;

    public CartController(CartService cartService) {
        this.cartService = cartService;
    }

    // GET /cart (No query param needed! Gets cart of the authenticated user)
    @GetMapping
    public CartResponseDTO getCart(Authentication authentication) {
        String username = authentication.getName();
        Cart cart = cartService.viewCart(username);
        return mapToResponseDTO(cart);
    }

    // POST /cart/items (Only productId and quantity in body)
    @PostMapping("/items")
    public CartResponseDTO addItem(@Valid @RequestBody AddItemRequestDTO request,
                                   Authentication authentication) {
        String username = authentication.getName();
        Cart cart = cartService.addItem(username, request.getProductId(), request.getQuantity());
        return mapToResponseDTO(cart);
    }

    // PUT /cart/items/{productId} (Only quantity in body)
    @PutMapping("/items/{productId}")
    public CartResponseDTO updateQuantity(@PathVariable String productId,
                                          @Valid @RequestBody UpdateQuantityRequestDTO request,
                                          Authentication authentication) {
        String username = authentication.getName();
        Cart cart = cartService.updateQuantity(username, productId, request.getQuantity());
        return mapToResponseDTO(cart);
    }

    // DELETE /cart/items/{productId} (No userId param needed!)
    @DeleteMapping("/items/{productId}")
    public CartResponseDTO removeItem(@PathVariable String productId,
                                      Authentication authentication) {
        String username = authentication.getName();
        Cart cart = cartService.removeItem(username, productId);
        return mapToResponseDTO(cart);
    }

    private CartResponseDTO mapToResponseDTO(Cart cart) {
        if (cart == null || cart.getItems() == null) {
            return new CartResponseDTO(cart != null ? cart.getUserId() : "", List.of());
        }
        List<CartItemResponseDTO> items = cart.getItems().stream()
                .map(i -> new CartItemResponseDTO(i.getProductId(), i.getName(), i.getQuantity(), i.getPrice()))
                .toList();
        return new CartResponseDTO(cart.getUserId(), items);
    }
}