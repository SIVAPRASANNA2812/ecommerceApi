package com.guvi.ecommerceApi.Service;

import com.guvi.ecommerceApi.Exception.BadRequestException;
import com.guvi.ecommerceApi.Exception.InsufficientStockException;
import com.guvi.ecommerceApi.Exception.ResourceNotFoundException;
import com.guvi.ecommerceApi.Model.Cart;
import com.guvi.ecommerceApi.Model.CartItems;
import com.guvi.ecommerceApi.Model.Product;
import com.guvi.ecommerceApi.Repository.CartRepository;
import com.guvi.ecommerceApi.Repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Optional;

@Service
public class CartServiceImpl implements CartService {

    @Autowired
    private CartRepository cartRepository;

    @Autowired
    private ProductRepository productRepository;

    public CartServiceImpl(CartRepository cartRepository, ProductRepository productRepository) {
        this.cartRepository = cartRepository;
        this.productRepository = productRepository;
    }

    @Override
    public Cart addItem(String userId, String productId, int quantity) {
        Product product = productRepository.findByProductId(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + productId));

        if (quantity <= 0) {
            throw new BadRequestException("Quantity must be greater than zero");
        }

        if (quantity > product.getStockQuantity()) {
            throw new InsufficientStockException("Exceeds available stock. Available: " + product.getStockQuantity());
        }

        Cart cart = cartRepository.findByUserId(userId);
        if (cart == null) {
            cart = new Cart();
            cart.setUserId(userId);
            cart.setItems(new ArrayList<>());
        }

        Optional<CartItems> existingItems = cart.getItems().stream()
                .filter(i -> i.getProductId().equals(productId))
                .findFirst();

        if (existingItems.isPresent()) {
            int newQty = existingItems.get().getQuantity() + quantity;
            if (newQty > product.getStockQuantity()) {
                throw new InsufficientStockException("Exceeds available stock. Available: " + product.getStockQuantity());
            }
            existingItems.get().setQuantity(newQty);

        } else {

            CartItems newItem = new CartItems(
                    productId,
                    product.getName(),
                    quantity,
                    product.getPrice() // BigDecimal
            );
            cart.getItems().add(newItem);
        }

        return cartRepository.save(cart);
    }

    @Override
    public Cart removeItem(String userId, String productId) {
        Cart cart = cartRepository.findByUserId(userId);
        if (cart == null) {
            throw new ResourceNotFoundException("Cart not found for user: " + userId);
        }
        cart.getItems().removeIf(i -> i.getProductId().equals(productId));
        return cartRepository.save(cart);
    }

    @Override
    public Cart updateQuantity(String userId, String productId, int quantity) {
        Product product = productRepository.findByProductId(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + productId));
        if (quantity <= 0) {
            throw new BadRequestException("Quantity must be greater than zero");
        }
        if (quantity > product.getStockQuantity()) {
            throw new InsufficientStockException("Not enough stock available. Available: " + product.getStockQuantity());
        }

        Cart cart = cartRepository.findByUserId(userId);
        if (cart == null) {
            throw new ResourceNotFoundException("Cart not found for user: " + userId);
        }

        Optional<CartItems> existingItem = cart.getItems().stream()
                .filter(i -> i.getProductId().equals(productId))
                .findFirst();

        if (existingItem.isEmpty()) {
            throw new ResourceNotFoundException("Item not found in cart for productId: " + productId);
        }

        existingItem.get().setQuantity(quantity);

        return cartRepository.save(cart);
    }


    @Override
    public Cart viewCart(String userId) {
        return cartRepository.findByUserId(userId);
    }
}
