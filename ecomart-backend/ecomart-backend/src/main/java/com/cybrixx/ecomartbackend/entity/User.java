package com.cybrixx.ecomartbackend.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import com.cybrixx.ecomartbackend.entity.enums.Role;
import lombok.*;
import java.util.List;

@Entity
@Table(name = "users")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String firstName;
    private String lastName;

    @Column(unique = true, nullable = false)
    private String email;

    @Column(nullable = false)
    private String password;

    private String phone;
    private String address;

    @Enumerated(EnumType.STRING)
    private Role role;

    /** Firebase Cloud Messaging device token of the user's most recent login (null when logged out). */
    @JsonIgnore
    @Column(length = 512)
    private String fcmToken;
}
