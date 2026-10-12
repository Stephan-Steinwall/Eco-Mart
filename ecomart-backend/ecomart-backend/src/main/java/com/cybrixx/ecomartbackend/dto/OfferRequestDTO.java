package com.cybrixx.ecomartbackend.dto;

import lombok.Data;

@Data
public class OfferRequestDTO {
    private String title;
    private String message;
    private Integer discountPercent;
    private Integer durationMinutes;
}
