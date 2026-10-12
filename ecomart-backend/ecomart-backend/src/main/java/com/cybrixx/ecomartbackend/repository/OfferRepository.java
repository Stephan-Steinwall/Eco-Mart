package com.cybrixx.ecomartbackend.repository;

import com.cybrixx.ecomartbackend.entity.Offer;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.List;

public interface OfferRepository extends JpaRepository<Offer, Long> {
    List<Offer> findAllByOrderByCreatedAtDesc();

    List<Offer> findByExpiresAtAfterOrderByExpiresAtAsc(Instant now);
}
