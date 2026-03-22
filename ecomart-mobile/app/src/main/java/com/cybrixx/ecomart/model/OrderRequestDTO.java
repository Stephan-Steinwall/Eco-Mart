package com.cybrixx.ecomart.model;

import java.util.List;

public class OrderRequestDTO {
    private String shippingAddress;
    private double latitude;  // NEW
    private double longitude; // NEW
    private List<OrderItemRequestDTO> items;

    // Update the constructor!
    public OrderRequestDTO(String shippingAddress, double latitude, double longitude, List<OrderItemRequestDTO> items) {
        this.shippingAddress = shippingAddress;
        this.latitude = latitude;
        this.longitude = longitude;
        this.items = items;
    }
}