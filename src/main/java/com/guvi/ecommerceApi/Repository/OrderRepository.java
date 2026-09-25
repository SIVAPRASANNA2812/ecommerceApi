package com.guvi.ecommerceApi.Repository;

import com.guvi.ecommerceApi.Model.Order;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderRepository extends MongoRepository<Order,String> {

    List<Order> findByUserId(String userId);
}
