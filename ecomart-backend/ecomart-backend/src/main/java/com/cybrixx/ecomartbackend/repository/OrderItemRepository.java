package com.cybrixx.ecomartbackend.repository;


import com.cybrixx.ecomartbackend.entity.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {
}
