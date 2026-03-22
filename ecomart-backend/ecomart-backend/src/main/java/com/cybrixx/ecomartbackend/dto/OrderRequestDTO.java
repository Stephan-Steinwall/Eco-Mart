package com.cybrixx.ecomartbackend.dto;

import lombok.Data;
import java.util.List;

@Data
public class OrderRequestDTO {
    private String shippingAddress;
    private Double latitude;  // NEW
    private Double longitude; // NEW
    private List<OrderItemRequestDTO> items;
}