package com.ecom.service;

import com.ecom.model.Cart;

import java.util.List;

public interface CartService {

    Cart saveCart(int productId, int userId);

    List<Cart> getCartByUser(int userId);

    int getCountCart(int userId);

    void updateQuantity(String symbol, int cid);

    void removeCartProducts(int userId);
}
