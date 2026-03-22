package com.cybrixx.ecomartbackend.controller;

import com.cybrixx.ecomartbackend.dto.OrderResponseDTO;
import com.cybrixx.ecomartbackend.dto.OrderStatusUpdateRequestDTO;
import com.cybrixx.ecomartbackend.entity.Order;
import com.cybrixx.ecomartbackend.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/orders")
@CrossOrigin(origins = "http://localhost:5173") // ADD THIS LINE!
@RequiredArgsConstructor
public class AdminOrderController {

    private final OrderService orderService;

    // 1. Get ALL orders in the system
    @GetMapping
    public ResponseEntity<List<OrderResponseDTO>> getAllOrders() {
        List<Order> orders = orderService.getAllOrders();

        List<OrderResponseDTO> dtos = orders.stream().map(order -> {
            OrderResponseDTO dto = new OrderResponseDTO();
            dto.setId(order.getId());
            dto.setOrderDate(order.getOrderDate());
            dto.setTotalAmount(order.getTotalAmount());
            dto.setStatus(order.getStatus().name());
            dto.setLatitude(order.getLatitude());
            dto.setLongitude(order.getLongitude());
            return dto;
        }).toList();

        return ResponseEntity.ok(dtos);
    }

    // 2. Update a specific order's status
    @PutMapping("/{id}/status")
    public ResponseEntity<?> updateOrderStatus(
            @PathVariable Long id,
            @RequestBody OrderStatusUpdateRequestDTO request) {
        try {
            Order updatedOrder = orderService.updateOrderStatus(id, request.getStatus());
            return ResponseEntity.ok().body("Order #" + id + " status successfully updated to: " + updatedOrder.getStatus().name());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Failed to update status: " + e.getMessage());
        }
    }
}