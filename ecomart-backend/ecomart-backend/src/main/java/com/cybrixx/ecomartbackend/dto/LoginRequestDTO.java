package com.cybrixx.ecomartbackend.dto;

import lombok.Data;

@Data
public class LoginRequestDTO {
    private String email;
    private String password;
}