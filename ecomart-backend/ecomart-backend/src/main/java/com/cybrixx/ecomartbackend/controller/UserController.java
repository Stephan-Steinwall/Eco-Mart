package com.cybrixx.ecomartbackend.controller;

import com.cybrixx.ecomartbackend.dto.FcmTokenDTO;
import com.cybrixx.ecomartbackend.dto.UserProfileDTO;
import com.cybrixx.ecomartbackend.entity.User;
import com.cybrixx.ecomartbackend.repository.UserRepository;
import com.cybrixx.ecomartbackend.service.FcmService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserRepository userRepository;
    private final FcmService fcmService;

    @GetMapping("/profile")
    public ResponseEntity<UserProfileDTO> getProfile(Principal principal) {
        User user = userRepository.findByEmail(principal.getName()).orElseThrow();
        UserProfileDTO dto = new UserProfileDTO(
                user.getFirstName(), user.getLastName(), user.getEmail(), user.getPhone(), user.getAddress()
        );
        return ResponseEntity.ok(dto);
    }

    @PutMapping("/profile")
    public ResponseEntity<?> updateProfile(Principal principal, @RequestBody UserProfileDTO dto) {
        User user = userRepository.findByEmail(principal.getName()).orElseThrow();

        user.setFirstName(dto.getFirstName());
        user.setLastName(dto.getLastName());
        user.setPhone(dto.getPhone());
        user.setAddress(dto.getAddress());
        // Note: We usually don't allow email updates without verification, so we leave it alone here.

        userRepository.save(user);
        return ResponseEntity.ok().body("Profile updated successfully!");
    }

    // The app calls this after login (and whenever Firebase rotates the token) so order updates
    // can be pushed to this specific device.
    @PutMapping("/fcm-token")
    public ResponseEntity<?> registerFcmToken(Principal principal, @RequestBody FcmTokenDTO dto) {
        if (dto.getToken() == null || dto.getToken().isBlank()) {
            return ResponseEntity.badRequest().body("Token is required");
        }
        // A device can hold on to a token Firebase has long forgotten (restored snapshot, backup restore, old
        // install). Storing it would make every push to this customer vanish, so refuse it: 410 tells the app
        // to discard the token and ask Firebase for a fresh one.
        if (fcmService.isTokenKnownInvalid(dto.getToken())) {
            return ResponseEntity.status(HttpStatus.GONE).body("Token is not registered with Firebase; request a new one");
        }
        User user = userRepository.findByEmail(principal.getName()).orElseThrow();
        userRepository.clearTokenFromOtherUsers(dto.getToken(), user.getId());
        user.setFcmToken(dto.getToken());
        userRepository.save(user);
        return ResponseEntity.ok().body("Device registered for notifications");
    }

    // Called on logout so a signed-out phone stops receiving this user's order updates.
    @DeleteMapping("/fcm-token")
    public ResponseEntity<?> unregisterFcmToken(Principal principal) {
        User user = userRepository.findByEmail(principal.getName()).orElseThrow();
        userRepository.clearToken(user.getId());
        return ResponseEntity.ok().body("Device unregistered from notifications");
    }
}