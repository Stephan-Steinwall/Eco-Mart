package com.cybrixx.ecomartbackend.service;

import com.cybrixx.ecomartbackend.dto.OrderItemRequestDTO;
import com.cybrixx.ecomartbackend.dto.OrderRequestDTO;
import com.cybrixx.ecomartbackend.entity.Order;
import com.cybrixx.ecomartbackend.entity.OrderItem;
import com.cybrixx.ecomartbackend.entity.Product;
import com.cybrixx.ecomartbackend.entity.User;
import com.cybrixx.ecomartbackend.entity.enums.OrderStatus;
import com.cybrixx.ecomartbackend.repository.OrderRepository;
import com.cybrixx.ecomartbackend.repository.ProductRepository;
import com.cybrixx.ecomartbackend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;

    // FIX 1: Added the method body to actually fetch the orders
    public List<Order> getOrdersByUserEmail(String email) {
        return orderRepository.findByUserEmailOrderByOrderDateDesc(email);
    }

    @Transactional
    public Order placeOrder(String userEmail, OrderRequestDTO requestDTO) {
        // 1. Find the logged-in user
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // 2. Initialize a new Order
        Order order = Order.builder()
                .user(user)
                .orderDate(LocalDateTime.now())
                .status(OrderStatus.PENDING)
                .shippingAddress(requestDTO.getShippingAddress())
                .latitude(requestDTO.getLatitude())   // NEW
                .longitude(requestDTO.getLongitude())
                .orderItems(new ArrayList<>())
                .build();

        BigDecimal totalAmount = BigDecimal.ZERO;

        // 3. Process each item in the cart
        for (OrderItemRequestDTO itemDto : requestDTO.getItems()) {
            Product product = productRepository.findById(itemDto.getProductId())
                    .orElseThrow(() -> new RuntimeException("Product not found: " + itemDto.getProductId()));

            // Calculate item total (Price * Quantity)
            BigDecimal itemTotal = product.getPrice().multiply(BigDecimal.valueOf(itemDto.getQuantity()));
            totalAmount = totalAmount.add(itemTotal);

            // Create OrderItem entity
            OrderItem orderItem = OrderItem.builder()
                    .order(order)
                    .product(product)
                    .quantity(itemDto.getQuantity())
                    .priceAtPurchase(product.getPrice())
                    .build();

            order.getOrderItems().add(orderItem);

            // Optional: Reduce product stock quantity here if needed
        }

        order.setTotalAmount(totalAmount);

        // 4. Save to database (Cascade will save OrderItems automatically)
        return orderRepository.save(order);
    }

    // --- ADMIN SPECIFIC METHODS ---

    @Transactional
    public Order updateOrderStatus(Long orderId, String newStatus) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found with ID: " + orderId));

        try {
            // Convert the string to the Enum (throws error if invalid string is sent)
            OrderStatus statusEnum = OrderStatus.valueOf(newStatus.toUpperCase());
            order.setStatus(statusEnum);
            return orderRepository.save(order);
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("Invalid order status: " + newStatus);
        }
    }

    // Bonus: Fetch all orders for the Admin dashboard
    public List<Order> getAllOrders() {
        return orderRepository.findAll();
    }
}