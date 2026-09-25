package com.guvi.ecommerceApi.Controller;

import com.guvi.ecommerceApi.DTO.ProductRequestDTO;
import com.guvi.ecommerceApi.DTO.ProductResponseDTO;
import com.guvi.ecommerceApi.Model.Product;
import com.guvi.ecommerceApi.Service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
public class ProductController {

    @Autowired
    private ProductService productService;

    @GetMapping("/products")
    public List<Product> getProducts(){
        return productService.getProducts();
    }

    @PostMapping("/products/addAll")
    public List<Product> addProducts(@RequestBody List<Product> products) {
        return productService.addProducts(products);
    }

    @PostMapping("/products/add")
    public ResponseEntity<ProductResponseDTO> addProduct(@RequestBody ProductRequestDTO productRequestDTO){

        Product savedProduct  = this.productService.addProduct(productRequestDTO);

        ProductResponseDTO response = new ProductResponseDTO(savedProduct.getId(),
                savedProduct.getProductId(),
                savedProduct.getName(),
                savedProduct.getPrice(),
                savedProduct.getCategory());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/products/update")
    public ResponseEntity<ProductResponseDTO> updateProduct(@RequestBody ProductRequestDTO productRequestDTO){
        Product savedProduct  = this.productService.updateProduct(productRequestDTO);

        ProductResponseDTO response = new ProductResponseDTO(savedProduct.getId(),
                savedProduct.getProductId(),
                savedProduct.getName(),
                savedProduct.getPrice(),
                savedProduct.getCategory());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }


    @DeleteMapping("/products/delete/{id}")
    public ResponseEntity<String> deleteProduct(@PathVariable String id){
        String response = this.productService.deleteProduct(id);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/products/{category}")
    public Product getProductBycategory(@PathVariable String category){
        return productService.getProductBycategory(category);
    }

    @GetMapping("/products/{id}")
    public Product getProductByid(@PathVariable String id){
        return productService.getProductsById(id);
    }


}
