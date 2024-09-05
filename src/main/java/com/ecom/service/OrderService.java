package com.ecom.service;

import com.ecom.model.OrderRequest;
import com.ecom.model.Product;
import com.ecom.model.ProductOrder;
import jakarta.mail.MessagingException;
import org.springframework.data.domain.Page;

import java.io.UnsupportedEncodingException;
import java.util.List;

public interface OrderService {

    void saveOrder(int userId, OrderRequest orderRequest) throws MessagingException, UnsupportedEncodingException;

    List<ProductOrder> getOrderByUser(int userId);

    ProductOrder updateOrderStatus(int id, String status);

    List<ProductOrder> getAllOrders();

    ProductOrder getOrderByOrderId(String orderId);

    List<Product> searchProduct(String search);

    Page<ProductOrder> getAllOrdersPagination(int pageNo, int pageSize);
}
