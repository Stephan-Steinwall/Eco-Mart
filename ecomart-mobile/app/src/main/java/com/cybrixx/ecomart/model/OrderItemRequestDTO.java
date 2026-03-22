package com.cybrixx.ecomart.model;


public class OrderItemRequestDTO {
    private Long productId;
    private int quantity;

    public OrderItemRequestDTO(Long productId, int quantity) {
        this.productId = productId;
        this.quantity = quantity;
    }
}
