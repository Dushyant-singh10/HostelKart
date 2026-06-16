package com.example.TTN_E_Commerce.Entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name="activation_token")
@Getter
@Setter
public class ActivationToken {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false,unique = true)
    String token;

    LocalDateTime expiryDate;

    @OneToOne
    @JoinColumn(name = "user_id",nullable = false)
    User user;
}
