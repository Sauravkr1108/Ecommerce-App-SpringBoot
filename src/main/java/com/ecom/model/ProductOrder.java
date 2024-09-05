package com.ecom.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.Date;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Entity
public class ProductOrder {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    private String orderId;

    private LocalDate orderDate;

    @ManyToOne
    private Product product;

    @ManyToOne
    private User user;

    @OneToOne(cascade = CascadeType.ALL)
    private OrderAddress orderAddress;

    private Double price;

    private int quantity;

    private String status;

    private String paymentType;

}
