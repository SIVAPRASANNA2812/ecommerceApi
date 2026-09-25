package com.guvi.ecommerceApi.Repository;


import com.guvi.ecommerceApi.Model.Product;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ProductRepository extends MongoRepository<Product,String> {

    Optional<Product> getProductById(String id);
    Optional<Product> findByProductId(String productId);
    Product getProductByCategory(String category);
}
