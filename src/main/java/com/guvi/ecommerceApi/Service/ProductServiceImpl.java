package com.guvi.ecommerceApi.Service;

import com.guvi.ecommerceApi.Exception.ResourceNotFoundException;
import com.guvi.ecommerceApi.Repository.ProductRepository;
import com.guvi.ecommerceApi.DTO.ProductRequestDTO;
import com.guvi.ecommerceApi.Model.Product;
import org.springframework.stereotype.Service;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import java.util.List;

@Service
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;

    public ProductServiceImpl(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    @Override
    public List<Product> getProducts(){
        return productRepository.findAll();
    }

    @Override
    public Product addProduct(ProductRequestDTO productRequestDTO){
        Product product = new Product();
        product.setProductId(productRequestDTO.getProductId());
        product.setName(productRequestDTO.getName());
        product.setDescription(productRequestDTO.getDescription());
        product.setPrice(productRequestDTO.getPrice());
        product.setStockQuantity(productRequestDTO.getStockQuantity());
        product.setCategory(productRequestDTO.getCategory());

        return productRepository.save(product);
    }

    @Override
    public Product updateProduct(ProductRequestDTO product) {
        Product existingProduct = productRepository.findByProductId(product.getProductId())
                .orElseThrow(() -> new ResourceNotFoundException("Cannot update. Product not found with productId: " + product.getProductId()));

        existingProduct.setName(product.getName());
        existingProduct.setDescription(product.getDescription());
        existingProduct.setPrice(product.getPrice());
        existingProduct.setStockQuantity(product.getStockQuantity());
        existingProduct.setCategory(product.getCategory());
        return productRepository.save(existingProduct);
    }

    @Override
    public String deleteProduct(String id) {
        if (!productRepository.existsById(id)) {
            throw new ResourceNotFoundException("Cannot delete. Product not found with id: " + id);
        }
        productRepository.deleteById(id);
        return "Deleted the product successfully !";
    }

    @Override
    public List<Product> addProducts(List<Product> products) {
        return productRepository.saveAll(products);
    }

    public Product getProductBycategory(String category){

        return productRepository.getProductByCategory(category);
    }

    @Override
    public Product getProductsById(String id) {
        return productRepository.getProductById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + id));
    }

    @Override
    public Page<Product> getProducts(int page, int size, String sortBy, String sortDir) {
        // Determine ascending or descending sort
        Sort sort = sortDir.equalsIgnoreCase("desc")
                ? Sort.by(sortBy).descending()
                : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);
        return productRepository.findAll(pageable);
    }

    @Override
    public Page<Product> getProductsByCategory(String category, int page, int size, String sortBy, String sortDir) {
        Sort sort = sortDir.equalsIgnoreCase("desc")
                ? Sort.by(sortBy).descending()
                : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);
        return productRepository.findByCategory(category, pageable);
    }

    @Override
    public Page<Product> searchByName(String name, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return productRepository.findByNameContainingIgnoreCase(name, pageable);
    }

    @Override
    public Page<Product> searchProductsByName(String name, int page, int size, String sortBy, String sortDir) {
        Sort sort = sortDir.equalsIgnoreCase("desc")
                ? Sort.by(sortBy).descending()
                : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);
        return productRepository.findByNameContainingIgnoreCase(name, pageable);
    }

}
