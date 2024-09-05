package com.ecom.repository;

import com.ecom.model.Cart;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CartRepository extends JpaRepository<Cart, Integer> {

    Cart findByProductIdAndUserId(int productId, int userId);

    int countByUserId(int userId);

    List<Cart> findByUserId(int userId);

    void deleteByUserId(int userId);

}
