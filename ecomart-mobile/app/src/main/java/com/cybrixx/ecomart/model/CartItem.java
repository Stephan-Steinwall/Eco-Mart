package com.cybrixx.ecomart.model;

public class CartItem {
    private int id; // SQLite auto-increment ID
    private Long productId; // The actual product ID from MySQL
    private String name;
    private double price;
    private int quantity;
    private String imageUrl;

    public CartItem(Long productId, String name, double price, int quantity, String imageUrl) {
        this.productId = productId;
        this.name = name;
        this.price = price;
        this.quantity = quantity;
        this.imageUrl = imageUrl;
    }

    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public Long getProductId() { return productId; }
    public String getName() { return name; }
    public double getPrice() { return price; }
    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }
    public String getImageUrl() { return imageUrl; }
}