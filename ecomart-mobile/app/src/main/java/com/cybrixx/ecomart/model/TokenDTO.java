package com.cybrixx.ecomart.model;

public class TokenDTO {
    private String token;
    private String role;

    private String firstName;
    private String lastName;

    private String email;


    public String getToken() { return token; }
    public String getRole() { return role; }

    public String getFirstName() { return firstName; }
    public String getLastName() { return lastName; }

    public String getEmail() { return email; }
}
