package com.cybrixx.ecomartbackend.controller;

import com.cybrixx.ecomartbackend.dto.OfferRequestDTO;
import com.cybrixx.ecomartbackend.dto.OfferResponseDTO;
import com.cybrixx.ecomartbackend.entity.Offer;
import com.cybrixx.ecomartbackend.repository.OfferRepository;
import com.cybrixx.ecomartbackend.service.FcmService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/offers")
@CrossOrigin(origins = "http://localhost:5173")
@RequiredArgsConstructor
public class AdminOfferController {

    private static final int MAX_DURATION_MINUTES = 60 * 24 * 30; // 30 days

    private final OfferRepository offerRepository;
    private final FcmService fcmService;

    @GetMapping
    public ResponseEntity<List<OfferResponseDTO>> getAllOffers() {
        return ResponseEntity.ok(offerRepository.findAllByOrderByCreatedAtDesc().stream()
                .map(OfferResponseDTO::from).toList());
    }

    // Saving an offer is the admin's "confirm": it goes live and every customer device is notified.
    @PostMapping
    public ResponseEntity<?> createOffer(@RequestBody OfferRequestDTO request) {
        String error = validate(request);
        if (error != null) {
            return ResponseEntity.badRequest().body(error);
        }

        Instant now = Instant.now();
        Duration duration = Duration.ofMinutes(request.getDurationMinutes());
        Offer offer = offerRepository.save(Offer.builder()
                .title(request.getTitle().trim())
                .message(request.getMessage().trim())
                .discountPercent(request.getDiscountPercent())
                .createdAt(now)
                .expiresAt(now.plus(duration))
                .build());

        Map<String, String> data = new HashMap<>();
        data.put("type", "OFFER");
        data.put("title", offer.getDiscountPercent() + "% OFF — " + offer.getTitle());
        data.put("body", offer.getMessage());
        data.put("offerId", String.valueOf(offer.getId()));

        // The offer is already saved; a push problem must not make the admin think the save failed.
        FcmService.PushResult result = fcmService.sendToTopic(FcmService.OFFERS_TOPIC, data, duration);

        OfferResponseDTO response = OfferResponseDTO.from(offer);
        response.setPushStatus(switch (result) {
            case SENT -> "SENT";
            case DISABLED -> "DISABLED";
            default -> "FAILED";
        });
        return ResponseEntity.ok(response);
    }

    // End an offer early but keep it in the history list.
    @PostMapping("/{id}/end")
    public ResponseEntity<?> endOffer(@PathVariable Long id) {
        return offerRepository.findById(id).<ResponseEntity<?>>map(offer -> {
            offer.setExpiresAt(Instant.now());
            return ResponseEntity.ok(OfferResponseDTO.from(offerRepository.save(offer)));
        }).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteOffer(@PathVariable Long id) {
        if (!offerRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        offerRepository.deleteById(id);
        return ResponseEntity.ok().body("Offer deleted successfully");
    }

    private String validate(OfferRequestDTO r) {
        if (r.getTitle() == null || r.getTitle().isBlank() || r.getTitle().trim().length() > 80) {
            return "Title is required (max 80 characters)";
        }
        if (r.getMessage() == null || r.getMessage().isBlank() || r.getMessage().trim().length() > 240) {
            return "Message is required (max 240 characters)";
        }
        if (r.getDiscountPercent() == null || r.getDiscountPercent() < 1 || r.getDiscountPercent() > 90) {
            return "Discount must be between 1 and 90 percent";
        }
        if (r.getDurationMinutes() == null || r.getDurationMinutes() < 1 || r.getDurationMinutes() > MAX_DURATION_MINUTES) {
            return "Duration must be between 1 minute and 30 days";
        }
        return null;
    }
}
