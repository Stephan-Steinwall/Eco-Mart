package com.cybrixx.ecomartbackend.repository;


import com.cybrixx.ecomartbackend.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface OrderRepository extends JpaRepository<Order, Long> {
    List<Order> findByUserId(Long userId);
    List<Order> findByUserEmailOrderByOrderDateDesc(String email);
}