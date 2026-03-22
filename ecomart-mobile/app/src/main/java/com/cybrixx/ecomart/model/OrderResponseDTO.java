package com.cybrixx.ecomart.model;

public class OrderResponseDTO {
    private Long id;
    private String orderDate;
    private double totalAmount;
    private String status;
    private double latitude;  // NEW
    private double longitude; // NEW

    public Long getId() { return id; }
    public String getOrderDate() { return orderDate; }
    public double getTotalAmount() { return totalAmount; }
    public String getStatus() { return status; }
    public double getLatitude() { return latitude; }   // NEW
    public double getLongitude() { return longitude; } // NEW
}