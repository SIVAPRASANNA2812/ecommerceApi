package com.guvi.ecommerceApi.Service;

import com.guvi.ecommerceApi.Repository.ProductRepository;
import com.guvi.ecommerceApi.DTO.ProductRequestDTO;
import com.guvi.ecommerceApi.Model.Product;
import org.springframework.stereotype.Service;

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
    public Product updateProduct(ProductRequestDTO product){
        Product updatedProduct = new Product(null, product.getProductId(),product.getName(), product.getDescription(), product.getPrice(), product.getStockQuantity(), product.getCategory());
        return productRepository.save(updatedProduct);
    }

    @Override
    public String deleteProduct(String id){
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

    public Product getProductsById(String id){
        return productRepository.getProductById(id).orElse(null);
    }

}
