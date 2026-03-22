package com.cybrixx.ecomartbackend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TokenDTO {
    private String token;
    private String role;
    private String firstName; // Add this
    private String lastName;  // Add this
    private String email;     // Add this
}
