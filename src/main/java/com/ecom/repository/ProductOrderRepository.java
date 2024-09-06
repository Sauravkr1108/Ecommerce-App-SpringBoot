package com.ecom.repository;

import com.ecom.model.ProductOrder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

public interface ProductOrderRepository extends JpaRepository<ProductOrder, Integer> {

    List<ProductOrder> findByUserId(int userId);

    ProductOrder findByOrderId(String orderId);
}
