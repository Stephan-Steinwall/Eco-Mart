package com.cybrixx.ecomartbackend.controller;

import com.cybrixx.ecomartbackend.dto.OfferResponseDTO;
import com.cybrixx.ecomartbackend.repository.OfferRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.List;

@RestController
@RequestMapping("/api/offers")
@RequiredArgsConstructor
public class OfferController {

    private final OfferRepository offerRepository;

    // Offers that haven't expired yet, soonest-ending first. Used by the app's home banner.
    @GetMapping("/active")
    public ResponseEntity<List<OfferResponseDTO>> getActiveOffers() {
        return ResponseEntity.ok(offerRepository.findByExpiresAtAfterOrderByExpiresAtAsc(Instant.now()).stream()
                .map(OfferResponseDTO::from).toList());
    }
}
