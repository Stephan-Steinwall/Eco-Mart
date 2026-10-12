package com.cybrixx.ecomartbackend.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

/** A time-limited promotion created in the admin panel and announced to customers by push. */
@Entity
@Table(name = "offers")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Offer {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 80)
    private String title;

    @Column(nullable = false, length = 240)
    private String message;

    @Column(nullable = false)
    private Integer discountPercent;

    @Column(nullable = false)
    private Instant createdAt;

    // Stored as an absolute instant so the countdown is correct in every time zone.
    @Column(nullable = false)
    private Instant expiresAt;
}
