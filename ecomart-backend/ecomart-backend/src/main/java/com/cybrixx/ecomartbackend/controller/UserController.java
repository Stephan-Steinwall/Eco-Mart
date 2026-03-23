package com.cybrixx.ecomartbackend.controller;

import com.cybrixx.ecomartbackend.dto.UserProfileDTO;
import com.cybrixx.ecomartbackend.entity.User;
import com.cybrixx.ecomartbackend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserRepository userRepository;

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
}