package com.cybrixx.ecomartbackend.repository;


import com.cybrixx.ecomartbackend.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductRepository extends JpaRepository<Product, Long> {
}