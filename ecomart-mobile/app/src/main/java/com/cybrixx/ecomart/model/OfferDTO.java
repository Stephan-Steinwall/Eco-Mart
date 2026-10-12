package com.cybrixx.ecomart.model;

public class OfferDTO {
    private Long id;
    private String title;
    private String message;
    private int discountPercent;
    // Computed by the server so a wrong device clock or time zone can't skew the countdown.
    private long secondsRemaining;

    public Long getId() { return id; }
    public String getTitle() { return title; }
    public String getMessage() { return message; }
    public int getDiscountPercent() { return discountPercent; }
    public long getSecondsRemaining() { return secondsRemaining; }
}
