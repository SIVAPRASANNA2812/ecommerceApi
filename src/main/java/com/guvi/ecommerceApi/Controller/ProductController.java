package com.guvi.ecommerceApi.Controller;

import com.guvi.ecommerceApi.DTO.ProductRequestDTO;
import com.guvi.ecommerceApi.DTO.ProductResponseDTO;
import com.guvi.ecommerceApi.Model.Product;
import com.guvi.ecommerceApi.Service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
public class ProductController {

    @Autowired
    private ProductService productService;

    // 1. Paginated Product List (Default: page 0, size 10, sorted by name ascending)
    // GET /api/products?page=0&size=10&sortBy=price&sortDir=asc
    @GetMapping("/products")
    public ResponseEntity<Page<Product>> getProducts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "name") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir) {
        Page<Product> products = productService.getProducts(page, size, sortBy, sortDir);
        return ResponseEntity.ok(products);
    }

    // 2. Paginated Products by Category
    // GET /api/products/category/Electronics?page=0&size=5&sortBy=price&sortDir=desc
    @GetMapping("/products/category/{category}")
    public ResponseEntity<Page<Product>> getProductsByCategory(
            @PathVariable String category,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "name") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir) {
        Page<Product> products = productService.getProductsByCategory(category, page, size, sortBy, sortDir);
        return ResponseEntity.ok(products);
    }

    // 3. Get single product by ID
    @GetMapping("/products/{id}")
    public Product getProductByid(@PathVariable String id){
        return productService.getProductsById(id);
    }

    // 4. Add a single product
    @PostMapping("/products/add")
    public ResponseEntity<ProductResponseDTO> addProduct(@RequestBody ProductRequestDTO productRequestDTO){
        Product savedProduct = this.productService.addProduct(productRequestDTO);

        ProductResponseDTO response = new ProductResponseDTO(
                savedProduct.getId(),
                savedProduct.getProductId(),
                savedProduct.getName(),
                savedProduct.getPrice(),
                savedProduct.getCategory());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // 5. Bulk add products
    @PostMapping("/products/addAll")
    public List<Product> addProducts(@RequestBody List<Product> products) {
        return productService.addProducts(products);
    }

    // 6. Update product
    @PutMapping("/products/update")
    public ResponseEntity<ProductResponseDTO> updateProduct(@RequestBody ProductRequestDTO productRequestDTO){
        Product savedProduct = this.productService.updateProduct(productRequestDTO);

        ProductResponseDTO response = new ProductResponseDTO(
                savedProduct.getId(),
                savedProduct.getProductId(),
                savedProduct.getName(),
                savedProduct.getPrice(),
                savedProduct.getCategory());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // 7. Delete product
    @DeleteMapping("/products/delete/{id}")
    public ResponseEntity<String> deleteProduct(@PathVariable String id){
        String response = this.productService.deleteProduct(id);
        return ResponseEntity.ok(response);
    }

    // GET /api/products/search?name=laptop&page=0&size=10&sortBy=price&sortDir=asc
    @GetMapping("/products/search")
    public ResponseEntity<Page<Product>> searchProductsByName(
            @RequestParam String name,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "name") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir) {
        Page<Product> products = productService.searchProductsByName(name, page, size, sortBy, sortDir);
        return ResponseEntity.ok(products);
    }
}