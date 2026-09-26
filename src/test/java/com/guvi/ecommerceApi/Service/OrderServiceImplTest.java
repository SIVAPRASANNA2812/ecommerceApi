package com.guvi.ecommerceApi.Service;

import com.guvi.ecommerceApi.DTO.OrderResponseDTO;
import com.guvi.ecommerceApi.Entity.OrderStatus;
import com.guvi.ecommerceApi.Exception.BadRequestException;
import com.guvi.ecommerceApi.Exception.InsufficientStockException;
import com.guvi.ecommerceApi.Exception.InvalidOrderStateException;
import com.guvi.ecommerceApi.Exception.ResourceNotFoundException;
import com.guvi.ecommerceApi.Model.*;
import com.guvi.ecommerceApi.Repository.CartRepository;
import com.guvi.ecommerceApi.Repository.OrderRepository;
import com.guvi.ecommerceApi.Repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class OrderServiceImplTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private CartRepository cartRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private MongoTemplate mongoTemplate;

    @InjectMocks
    private OrderServiceImpl orderService;

    private Cart sampleCart;
    private Product sampleProduct;
    private Order sampleOrder;

    @BeforeEach
    void setUp() {
        // Product with 10 units in stock
        sampleProduct = new Product(
                "mongo-p1",
                "P101",
                "Mechanical Keyboard",
                "Tactile Switches",
                BigDecimal.valueOf(100.00),
                10,
                "Electronics"
        );

        // Cart with 2 Keyboards ($100 each -> Total $200)
        sampleCart = new Cart();
        sampleCart.setId("cart-1");
        sampleCart.setUserId("user123");
        List<CartItems> items = new ArrayList<>();
        items.add(new CartItems("P101", "Mechanical Keyboard", 2, BigDecimal.valueOf(100.00)));
        sampleCart.setItems(items);

        // Sample placed order
        List<OrderItem> orderItems = List.of(new OrderItem("P101", 2, BigDecimal.valueOf(100.00)));
        sampleOrder = new Order("ORD-999", "user123", orderItems, BigDecimal.valueOf(200.00), OrderStatus.PLACED);
    }

    // -------------------------------------------------------------
    // TEST 1: Place Order - Empty Cart (400 Bad Request)
    // -------------------------------------------------------------
    @Test
    @DisplayName("Should throw BadRequestException when attempting to checkout an empty cart")
    void placeOrder_EmptyCart_ThrowsBadRequestException() {
        Cart emptyCart = new Cart("cart-empty", "user123", new ArrayList<>());
        when(cartRepository.findByUserId("user123")).thenReturn(emptyCart);

        BadRequestException ex = assertThrows(
                BadRequestException.class,
                () -> orderService.placeOrder("user123")
        );

        assertEquals("Cart is empty, cannot place order", ex.getMessage());
        verify(orderRepository, never()).save(any(Order.class));
    }

    // -------------------------------------------------------------
    // TEST 2: Place Order - Insufficient Warehouse Stock (400)
    // -------------------------------------------------------------
    @Test
    @DisplayName("Should throw InsufficientStockException when stock deduction fails in Mongo")
    void placeOrder_InsufficientStock_ThrowsInsufficientStockException() {
        when(cartRepository.findByUserId("user123")).thenReturn(sampleCart);
        when(productRepository.findByProductId("P101")).thenReturn(Optional.of(sampleProduct));

        // Simulate Mongo findAndModify returning null (indicating atomic stock deduction failed)
        when(mongoTemplate.findAndModify(any(Query.class), any(Update.class), eq(Product.class))).thenReturn(null);

        assertThrows(
                InsufficientStockException.class,
                () -> orderService.placeOrder("user123")
        );

        verify(orderRepository, never()).save(any(Order.class));
    }

    // -------------------------------------------------------------
    // TEST 3: Place Order - Success (Happy Path)
    // -------------------------------------------------------------
    @Test
    @DisplayName("Should create order, calculate total amount, and clear cart upon successful checkout")
    void placeOrder_Success() {
        when(cartRepository.findByUserId("user123")).thenReturn(sampleCart);
        when(productRepository.findByProductId("P101")).thenReturn(Optional.of(sampleProduct));
        when(mongoTemplate.findAndModify(any(Query.class), any(Update.class), eq(Product.class))).thenReturn(sampleProduct);

        // Simulate saving the order
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> {
            Order o = invocation.getArgument(0);
            o.setOrderId("ORD-GENERATED-1");
            return o;
        });

        OrderResponseDTO response = orderService.placeOrder("user123");

        // Verify order response
        assertNotNull(response);
        assertEquals("ORD-GENERATED-1", response.getOrderId());
        assertEquals("user123", response.getUserId());
        assertEquals(OrderStatus.PLACED, response.getStatus());
        assertEquals(BigDecimal.valueOf(200.00), response.getTotalAmount()); // 2 * $100 = $200

        // Verify cart was cleared
        assertEquals(0, sampleCart.getItems().size());
        verify(cartRepository, times(1)).save(sampleCart);
    }

    // -------------------------------------------------------------
    // TEST 4: Cancel Order - Order Not Found (404)
    // -------------------------------------------------------------
    @Test
    @DisplayName("Should throw ResourceNotFoundException when cancelling a non-existent order")
    void cancelOrder_NotFound_ThrowsException() {
        when(orderRepository.findById("INVALID_ORD")).thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> orderService.cancelOrder("INVALID_ORD")
        );

        verify(orderRepository, never()).save(any(Order.class));
    }

    // -------------------------------------------------------------
    // TEST 5: Cancel Order - Already Cancelled (Invalid State 400)
    // -------------------------------------------------------------
    @Test
    @DisplayName("Should throw InvalidOrderStateException when cancelling an already cancelled order")
    void cancelOrder_AlreadyCancelled_ThrowsInvalidOrderStateException() {
        sampleOrder.setStatus(OrderStatus.CANCELLED);
        when(orderRepository.findById("ORD-999")).thenReturn(Optional.of(sampleOrder));

        InvalidOrderStateException ex = assertThrows(
                InvalidOrderStateException.class,
                () -> orderService.cancelOrder("ORD-999")
        );

        assertEquals("Order is already cancelled", ex.getMessage());
        verify(mongoTemplate, never()).updateFirst(any(Query.class), any(Update.class), eq(Product.class));
    }

    // -------------------------------------------------------------
    // TEST 6: Cancel Order - Shipped Order Cannot Be Cancelled (400)
    // -------------------------------------------------------------
    @Test
    @DisplayName("Should throw InvalidOrderStateException when cancelling a shipped order")
    void cancelOrder_ShippedOrder_ThrowsInvalidOrderStateException() {
        sampleOrder.setStatus(OrderStatus.SHIPPED);
        when(orderRepository.findById("ORD-999")).thenReturn(Optional.of(sampleOrder));

        InvalidOrderStateException ex = assertThrows(
                InvalidOrderStateException.class,
                () -> orderService.cancelOrder("ORD-999")
        );

        assertEquals("Shipped orders cannot be cancelled", ex.getMessage());
        verify(mongoTemplate, never()).updateFirst(any(Query.class), any(Update.class), eq(Product.class));
    }

    // -------------------------------------------------------------
    // TEST 7: Cancel Order - Success (Stock Restored & Status Updated)
    // -------------------------------------------------------------
    @Test
    @DisplayName("Should cancel order and restore stock in database")
    void cancelOrder_Success() {
        when(orderRepository.findById("ORD-999")).thenReturn(Optional.of(sampleOrder));
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> invocation.getArgument(0));

        OrderResponseDTO response = orderService.cancelOrder("ORD-999");

        assertNotNull(response);
        assertEquals(OrderStatus.CANCELLED, response.getStatus());

        // Verify stock restoration was invoked for the items in the order
        verify(mongoTemplate, times(1)).updateFirst(any(Query.class), any(Update.class), eq(Product.class));
        verify(orderRepository, times(1)).save(sampleOrder);
    }
}