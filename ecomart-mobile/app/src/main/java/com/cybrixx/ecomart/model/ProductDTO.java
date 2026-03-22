package com.cybrixx.ecomart.model;

public class ProductDTO {
    private Long id;
    private String name;
    private String description;
    private double price;
    private String category;
    private String imageUrl;

    // Getters
    public Long getId() { return id; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public double getPrice() { return price; }
    public String getCategory() { return category; }
    public String getImageUrl() { return imageUrl; }
}
