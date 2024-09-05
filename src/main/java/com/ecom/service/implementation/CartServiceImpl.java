package com.ecom.service.implementation;

import com.ecom.model.Cart;
import com.ecom.model.Product;
import com.ecom.model.User;
import com.ecom.repository.CartRepository;
import com.ecom.repository.ProductRepository;
import com.ecom.repository.UserRepository;
import com.ecom.service.CartService;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

import java.util.ArrayList;
import java.util.List;

@Service
public class CartServiceImpl implements CartService {

    @Autowired
    private CartRepository cartRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProductRepository productRepository;

    @Override
    public Cart saveCart(int productId, int userId) {
        User user = userRepository.findById(userId).get();
        Product product = productRepository.findById(productId).get();
        Cart cartStatus = cartRepository.findByProductIdAndUserId(productId, userId);

        Cart cart = null;
        if(ObjectUtils.isEmpty(cartStatus)){
            cart = new Cart();
            cart.setProduct(product);
            cart.setUser(user);
            cart.setQuantity(1);
            cart.setTotalPrice(product.getDiscountPrice());
        }
        else {
            cart = cartStatus;
            cart.setQuantity(cart.getQuantity() + 1);
            cart.setTotalPrice(cart.getQuantity() * cart.getProduct().getDiscountPrice());
        }

        return cartRepository.save(cart);
    }

    @Override
    public List<Cart> getCartByUser(int userId) {

        List<Cart> carts = cartRepository.findByUserId(userId);
        List<Cart> updatedCarts = new ArrayList<>();
        double totalOrderPrice = 0.0;
        for(Cart c : carts){
            double totalPrice = c.getProduct().getDiscountPrice() * c.getQuantity();
            totalOrderPrice += totalPrice;
            c.setTotalPrice(totalPrice);
            c.setTotalOrderPrice(totalOrderPrice);
            updatedCarts.add(c);
        }
        return updatedCarts;
    }

    @Override
    public int getCountCart(int userId) {
        return cartRepository.countByUserId(userId);
    }

    @Override
    public void updateQuantity(String symbol, int cid) {

        Cart cart = cartRepository.findById(cid).get();
        int updateQuantity;

        if(symbol.equalsIgnoreCase("minus")) {
            updateQuantity = cart.getQuantity() - 1;
            if(updateQuantity <= 0)
                cartRepository.delete(cart);
            else {
                cart.setQuantity(updateQuantity);
                cartRepository.save(cart);
            }
        }
        else {
            updateQuantity = cart.getQuantity() + 1;
            cart.setQuantity(updateQuantity);
            cartRepository.save(cart);
        }
    }

    @Override
    @Transactional
    public void removeCartProducts(int userId) {
        cartRepository.deleteByUserId(userId);
    }


}
