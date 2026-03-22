package com.cybrixx.ecomart.model;

public class RegisterRequestDTO {
    private String firstName;
    private String lastName;
    private String email;
    private String password;

    public RegisterRequestDTO(String firstName, String lastName, String email, String password) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.password = password;
    }
    // Getters and Setters
}
