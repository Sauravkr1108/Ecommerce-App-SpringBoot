package com.ecom.util;

public enum OrderStatus {

    IN_PROGRESS(1, "In Progress"),
    ORDER_RECEIVED(2, "Product Packed"),
    PRODUCT_PACKED(3, "Out of delivery"),
    OUT_OF_DELIVERY(4, "Delivered"),
    DELIVERED(5, "Order Received"),
    CANCELLED(6, "Cancelled"),
    SUCCESS(7, "Success");

    private int id;
    private String statusName;

    OrderStatus(int id, String statusName) {
        this.id = id;
        this.statusName = statusName;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getStatusName() {
        return statusName;
    }

    public void setStatusName(String statusName) {
        this.statusName = statusName;
    }
}
