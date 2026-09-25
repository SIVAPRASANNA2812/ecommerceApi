package com.guvi.ecommerceApi.Service;

import com.guvi.ecommerceApi.Model.Cart;

public interface CartService {

    Cart addItem(String userId, String productId, int quantity);
    Cart removeItem(String userId, String productId);
    Cart updateQuantity(String userId, String productId, int quantity);
    Cart viewCart(String userId);
}
