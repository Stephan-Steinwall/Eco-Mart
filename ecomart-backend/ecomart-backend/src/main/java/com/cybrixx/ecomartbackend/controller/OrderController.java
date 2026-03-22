package com.cybrixx.ecomartbackend.controller;

import com.cybrixx.ecomartbackend.dto.OrderRequestDTO;
import com.cybrixx.ecomartbackend.dto.OrderResponseDTO; // FIX 2: Added missing import
import com.cybrixx.ecomartbackend.entity.Order;           // FIX 2: Added missing import
import com.cybrixx.ecomartbackend.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List; // FIX 2: Added missing import

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @PostMapping
    public ResponseEntity<?> placeOrder(@RequestBody OrderRequestDTO requestDTO, Principal principal) {
        try {
            // principal.getName() contains the email from the JWT token
            orderService.placeOrder(principal.getName(), requestDTO);
            return ResponseEntity.ok().body("Order placed successfully");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Failed to place order: " + e.getMessage());
        }
    }

    @GetMapping("/my-orders")
    public ResponseEntity<?> getMyOrders(Principal principal) {
        List<Order> orders = orderService.getOrdersByUserEmail(principal.getName());

        // Convert to DTOs
        List<OrderResponseDTO> dtos = orders.stream().map(order -> {
            OrderResponseDTO dto = new OrderResponseDTO();
            dto.setId(order.getId());
            dto.setOrderDate(order.getOrderDate());
            dto.setTotalAmount(order.getTotalAmount());
            dto.setStatus(order.getStatus().name());
            dto.setLatitude(order.getLatitude());   // NEW
            dto.setLongitude(order.getLongitude());
            return dto;
        }).toList();

        return ResponseEntity.ok(dtos);
    }
}