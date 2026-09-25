package com.guvi.ecommerceApi.Repository;

import com.guvi.ecommerceApi.Model.Cart;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CartRepository extends MongoRepository<Cart , String> {
    Cart findByUserId(String userId);
}
