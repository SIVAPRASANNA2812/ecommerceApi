package com.guvi.ecommerceApi.Service;

import com.guvi.ecommerceApi.Exception.BadRequestException;
import com.guvi.ecommerceApi.Exception.InsufficientStockException;
import com.guvi.ecommerceApi.Exception.ResourceNotFoundException;
import com.guvi.ecommerceApi.Model.Cart;
import com.guvi.ecommerceApi.Model.CartItems;
import com.guvi.ecommerceApi.Model.Product;
import com.guvi.ecommerceApi.Repository.CartRepository;
import com.guvi.ecommerceApi.Repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class CartServiceImplTest {

    @Mock
    private CartRepository cartRepository; // Mock dependency 1

    @Mock
    private ProductRepository productRepository; // Mock dependency 2

    @InjectMocks
    private CartServiceImpl cartService; // Class under test with both mocks injected

    private Product sampleProduct;
    private Cart sampleCart;

    @BeforeEach
    void setUp() {
        // Sample product with 10 units in stock
        sampleProduct = new Product(
                "mongo-prod-1",
                "P101",
                "Wireless Mouse",
                "Ergonomic Mouse",
                BigDecimal.valueOf(50.00),
                10,
                "Electronics"
        );

        // Sample cart with 1 existing item (quantity = 2)
        sampleCart = new Cart();
        sampleCart.setId("cart-1");
        sampleCart.setUserId("user123");
        List<CartItems> items = new ArrayList<>();
        items.add(new CartItems("P101", "Wireless Mouse", 2, BigDecimal.valueOf(50.00)));
        sampleCart.setItems(items);
    }

    // -------------------------------------------------------------
    // TEST 1: Add Item - Product Does Not Exist (404)
    // -------------------------------------------------------------
    @Test
    @DisplayName("Should throw ResourceNotFoundException when product is not found")
    void addItem_ProductNotFound_ThrowsException() {
        when(productRepository.findByProductId("INVALID_P")).thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> cartService.addItem("user123", "INVALID_P", 1)
        );

        // Verify cart was never saved
        verify(cartRepository, never()).save(any(Cart.class));
    }

    // -------------------------------------------------------------
    // TEST 2: Add Item - Zero or Negative Quantity (400)
    // -------------------------------------------------------------
    @Test
    @DisplayName("Should throw BadRequestException when quantity is zero or negative")
    void addItem_InvalidQuantity_ThrowsBadRequestException() {
        when(productRepository.findByProductId("P101")).thenReturn(Optional.of(sampleProduct));

        assertThrows(
                BadRequestException.class,
                () -> cartService.addItem("user123", "P101", 0)
        );

        verify(cartRepository, never()).save(any(Cart.class));
    }

    // -------------------------------------------------------------
    // TEST 3: Add Item - Quantity Exceeds Available Stock (400)
    // -------------------------------------------------------------
    @Test
    @DisplayName("Should throw InsufficientStockException when requested quantity exceeds stock")
    void addItem_ExceedsStock_ThrowsInsufficientStockException() {
        when(productRepository.findByProductId("P101")).thenReturn(Optional.of(sampleProduct));

        // Sample product only has 10 units in stock, requesting 15
        assertThrows(
                InsufficientStockException.class,
                () -> cartService.addItem("user123", "P101", 15)
        );

        verify(cartRepository, never()).save(any(Cart.class));
    }

    // -------------------------------------------------------------
    // TEST 4: Add Item - New Cart & New Item (Happy Path)
    // -------------------------------------------------------------
    @Test
    @DisplayName("Should create new cart and add item when user has no existing cart")
    void addItem_NewCart_Success() {
        when(productRepository.findByProductId("P101")).thenReturn(Optional.of(sampleProduct));
        when(cartRepository.findByUserId("user123")).thenReturn(null); // No cart yet
        when(cartRepository.save(any(Cart.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Cart result = cartService.addItem("user123", "P101", 3);

        assertNotNull(result);
        assertEquals("user123", result.getUserId());
        assertEquals(1, result.getItems().size());
        assertEquals("P101", result.getItems().get(0).getProductId());
        assertEquals(3, result.getItems().get(0).getQuantity());

        verify(cartRepository, times(1)).save(any(Cart.class));
    }

    // -------------------------------------------------------------
    // TEST 5: Add Item - Item Already in Cart (Combines Quantity)
    // -------------------------------------------------------------
    @Test
    @DisplayName("Should increment quantity when item already exists in cart")
    void addItem_ExistingItem_IncrementsQuantity() {
        when(productRepository.findByProductId("P101")).thenReturn(Optional.of(sampleProduct));
        when(cartRepository.findByUserId("user123")).thenReturn(sampleCart); // Cart already has 2 items
        when(cartRepository.save(any(Cart.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Adding 3 more items (2 + 3 = 5, which is <= 10 in stock)
        Cart result = cartService.addItem("user123", "P101", 3);

        assertNotNull(result);
        assertEquals(1, result.getItems().size());
        assertEquals(5, result.getItems().get(0).getQuantity()); // 2 + 3 = 5

        verify(cartRepository, times(1)).save(sampleCart);
    }

    // -------------------------------------------------------------
    // TEST 6: Remove Item - Cart Not Found (404)
    // -------------------------------------------------------------
    @Test
    @DisplayName("Should throw ResourceNotFoundException when removing item from non-existent cart")
    void removeItem_CartNotFound_ThrowsException() {
        when(cartRepository.findByUserId("unknown_user")).thenReturn(null);

        assertThrows(
                ResourceNotFoundException.class,
                () -> cartService.removeItem("unknown_user", "P101")
        );

        verify(cartRepository, never()).save(any(Cart.class));
    }

    // -------------------------------------------------------------
    // TEST 7: Remove Item - Success
    // -------------------------------------------------------------
    @Test
    @DisplayName("Should remove item from cart successfully")
    void removeItem_Success() {
        when(cartRepository.findByUserId("user123")).thenReturn(sampleCart);
        when(cartRepository.save(any(Cart.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Cart result = cartService.removeItem("user123", "P101");

        assertNotNull(result);
        assertEquals(0, result.getItems().size()); // Item removed
        verify(cartRepository, times(1)).save(sampleCart);
    }
}