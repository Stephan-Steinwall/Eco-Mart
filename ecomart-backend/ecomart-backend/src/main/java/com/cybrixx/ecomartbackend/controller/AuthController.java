package com.cybrixx.ecomartbackend.controller;

import com.cybrixx.ecomartbackend.config.security.JwtUtils;
import com.cybrixx.ecomartbackend.dto.LoginRequestDTO;
import com.cybrixx.ecomartbackend.dto.RegisterRequestDTO;
import com.cybrixx.ecomartbackend.dto.TokenDTO;
import com.cybrixx.ecomartbackend.entity.User;
import com.cybrixx.ecomartbackend.entity.enums.Role;
import com.cybrixx.ecomartbackend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "http://localhost:5173") // ADD THIS LINE!
@RequiredArgsConstructor
public class AuthController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtils jwtUtils;
    private final AuthenticationManager authenticationManager;

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterRequestDTO request) {
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            return ResponseEntity.badRequest().body("Email already exists");
        }

        User user = User.builder()
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(Role.ROLE_USER) // Default role for new signups
                .build();

        userRepository.save(user);
        String token = jwtUtils.generateToken(user.getEmail());

        // Pack the real user data into the response
        TokenDTO response = TokenDTO.builder()
                .token(token)
                .role(user.getRole().name())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .email(user.getEmail())
                .build();

        return ResponseEntity.ok(response);
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequestDTO request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
        );

        User user = userRepository.findByEmail(request.getEmail()).orElseThrow();
        String token = jwtUtils.generateToken(user.getEmail());

        // Pack the real user data into the response
        TokenDTO response = TokenDTO.builder()
                .token(token)
                .role(user.getRole().name())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .email(user.getEmail())
                .build();

        return ResponseEntity.ok(response);
    }
}