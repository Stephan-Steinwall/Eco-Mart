package com.cybrixx.ecomart.model;
public class LoginRequestDTO {
    private String email;
    private String password;

    public LoginRequestDTO(String email, String password) {
        this.email = email;
        this.password = password;
    }
    // Getters and Setters
}
