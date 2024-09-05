package com.ecom.service.implementation;

import com.ecom.model.*;
import com.ecom.repository.CartRepository;
import com.ecom.repository.ProductOrderRepository;
import com.ecom.repository.ProductRepository;
import com.ecom.service.OrderService;
import com.ecom.util.CommonUtil;
import com.ecom.util.OrderStatus;
import jakarta.mail.MessagingException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.io.UnsupportedEncodingException;
import java.time.LocalDate;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@Service
public class OrderServiceImpl implements OrderService {

    @Autowired
    private ProductOrderRepository productOrderRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CartRepository cartRepository;

    @Autowired
    private CommonUtil commonUtil;

    @Override
    public void saveOrder(int userId, OrderRequest orderRequest) throws MessagingException, UnsupportedEncodingException {
        List<Cart> carts = cartRepository.findByUserId(userId);
        for(Cart cart : carts){
            ProductOrder order = new ProductOrder();
            order.setOrderId(UUID.randomUUID().toString());
            order.setOrderDate(LocalDate.now());
            order.setProduct(cart.getProduct());
            order.setPrice(cart.getProduct().getDiscountPrice());
            order.setQuantity(cart.getQuantity());
            order.setUser(cart.getUser());
            order.setStatus(OrderStatus.IN_PROGRESS.getStatusName());
            order.setPaymentType(orderRequest.getPaymentType());
            
            OrderAddress address = new OrderAddress();
            address.setFirstName(orderRequest.getFirstName());
            address.setLastName(orderRequest.getLastName());
            address.setEmail(orderRequest.getEmail());
            address.setMobileNo(orderRequest.getMobileNo());
            address.setAddress(orderRequest.getAddress());
            address.setCity(orderRequest.getCity());
            address.setState(orderRequest.getState());
            address.setPincode(orderRequest.getPincode());
            
            order.setOrderAddress(address);

            productOrderRepository.save(order);
            commonUtil.sendMailForProductOrder(order, "Success");
        }
    }

    @Override
    public List<ProductOrder> getOrderByUser(int userId) {
        return productOrderRepository.findByUserId(userId);
    }

    @Override
    public ProductOrder updateOrderStatus(int id, String status) {
        if(productOrderRepository.findById(id).isPresent()){
            ProductOrder order = productOrderRepository.findById(id).get();
            order.setStatus(status);
            productOrderRepository.save(order);
            return order;
        }
        return null;
    }

    @Override
    public List<ProductOrder> getAllOrders() {
        return productOrderRepository.findAll();
    }

    @Override
    public ProductOrder getOrderByOrderId(String orderId) {
        return productOrderRepository.findByOrderId(orderId);
    }

    @Override
    public List<Product> searchProduct(String search) {
        return productRepository.findByTitleContainingIgnoreCaseOrCategoryContainingIgnoreCase(search, search);
    }

    @Override
    public Page<ProductOrder> getAllOrdersPagination(int pageNo, int pageSize) {
        Pageable pageable = PageRequest.of(pageNo, pageSize);
        return productOrderRepository.findAll(pageable);
    }
}
