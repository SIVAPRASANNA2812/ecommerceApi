package com.guvi.ecommerceApi.Service;

import com.guvi.ecommerceApi.Exception.ResourceNotFoundException;
import com.guvi.ecommerceApi.Model.Product;
import com.guvi.ecommerceApi.Repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ProductServiceImplTest {

    @Mock
    private ProductRepository productRepository; // Fake repository

    @InjectMocks
    private ProductServiceImpl productService;   // Real service under test

    private Product sampleProduct;

    @BeforeEach
    void setUp() {
        // Runs before each test to prepare fresh sample data
        sampleProduct = new Product(
                "mongo-id-123",
                "P101",
                "Wireless Mouse",
                "RGB Ergonomic Mouse",
                BigDecimal.valueOf(49.99),
                20,
                "Electronics"
        );
    }

    // ---------------------------------------------------------
    // TEST 1: Happy Path - Product Found by ID
    // ---------------------------------------------------------
    @Test
    @DisplayName("Should return product when valid ID is provided")
    void getProductsById_WhenProductExists_ReturnsProduct() {
        // 1. Arrange: Tell mock repo to return sampleProduct when searching "mongo-id-123"
        when(productRepository.getProductById("mongo-id-123")).thenReturn(Optional.of(sampleProduct));

        // 2. Act: Call the service method
        Product result = productService.getProductsById("mongo-id-123");

        // 3. Assert: Verify the result
        assertNotNull(result);
        assertEquals("P101", result.getProductId());
        assertEquals("Wireless Mouse", result.getName());
        assertEquals(BigDecimal.valueOf(49.99), result.getPrice());

        // Verify that the repository method was called exactly once
        verify(productRepository, times(1)).getProductById("mongo-id-123");
    }

    // ---------------------------------------------------------
    // TEST 2: Exception Path - Product Not Found
    // ---------------------------------------------------------
    @Test
    @DisplayName("Should throw ResourceNotFoundException when product ID does not exist")
    void getProductsById_WhenProductDoesNotExist_ThrowsResourceNotFoundException() {
        // 1. Arrange: Tell mock repo to return empty Optional
        when(productRepository.getProductById("non-existent-id")).thenReturn(Optional.empty());

        // 2. Act & Assert: Verify that calling the method throws ResourceNotFoundException
        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> productService.getProductsById("non-existent-id")
        );

        // Verify the exception message matches what we wrote in ProductServiceImpl
        assertEquals("Product not found with id: non-existent-id", exception.getMessage());

        // Verify repo was called
        verify(productRepository, times(1)).getProductById("non-existent-id");
    }

    // ---------------------------------------------------------
    // TEST 3: Delete Product - Product Does Not Exist
    // ---------------------------------------------------------
    @Test
    @DisplayName("Should throw ResourceNotFoundException when attempting to delete non-existent product")
    void deleteProduct_WhenProductDoesNotExist_ThrowsException() {
        // 1. Arrange: existsById returns false
        when(productRepository.existsById("invalid-id")).thenReturn(false);

        // 2. Act & Assert: Ensure ResourceNotFoundException is thrown
        assertThrows(
                ResourceNotFoundException.class,
                () -> productService.deleteProduct("invalid-id")
        );

        // Verify that deleteById was NEVER called because validation failed first!
        verify(productRepository, never()).deleteById("invalid-id");
    }

    // ---------------------------------------------------------
    // TEST 4: Pagination & Sorting Test
    // ---------------------------------------------------------
    @Test
    @DisplayName("Should return paginated products")
    void getProducts_ReturnsPaginatedProducts() {
        // 1. Arrange
        Page<Product> page = new PageImpl<>(List.of(sampleProduct));
        when(productRepository.findAll(any(Pageable.class))).thenReturn(page);

        // 2. Act
        Page<Product> result = productService.getProducts(0, 10, "price", "asc");

        // 3. Assert
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals("Wireless Mouse", result.getContent().get(0).getName());
        verify(productRepository, times(1)).findAll(any(Pageable.class));
    }
}