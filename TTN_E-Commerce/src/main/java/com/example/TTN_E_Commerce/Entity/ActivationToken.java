package com.example.TTN_E_Commerce.Entity;

import com.example.TTN_E_Commerce.Enum.TokenType;
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

    @Column(nullable = false)
    String token;

    LocalDateTime expiryDate;

    @OneToOne
    @JoinColumn(name = "user_id",nullable = false)
    User user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TokenType tokenType = TokenType.SIGNUP;
}

