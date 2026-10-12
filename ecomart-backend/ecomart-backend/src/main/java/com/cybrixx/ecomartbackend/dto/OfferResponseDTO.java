package com.cybrixx.ecomartbackend.dto;

import com.cybrixx.ecomartbackend.entity.Offer;
import lombok.Data;

import java.time.Duration;
import java.time.Instant;

@Data
public class OfferResponseDTO {
    private Long id;
    private String title;
    private String message;
    private Integer discountPercent;
    private Instant createdAt;
    private Instant expiresAt;
    private boolean active;
    // Server-computed so devices with a wrong clock or time zone still show a correct countdown.
    private long secondsRemaining;
    // Only set on the create response: SENT, DISABLED (no Firebase key configured) or FAILED.
    private String pushStatus;

    public static OfferResponseDTO from(Offer offer) {
        OfferResponseDTO dto = new OfferResponseDTO();
        dto.setId(offer.getId());
        dto.setTitle(offer.getTitle());
        dto.setMessage(offer.getMessage());
        dto.setDiscountPercent(offer.getDiscountPercent());
        dto.setCreatedAt(offer.getCreatedAt());
        dto.setExpiresAt(offer.getExpiresAt());
        long remaining = Math.max(Duration.between(Instant.now(), offer.getExpiresAt()).getSeconds(), 0);
        dto.setSecondsRemaining(remaining);
        dto.setActive(remaining > 0);
        return dto;
    }
}
