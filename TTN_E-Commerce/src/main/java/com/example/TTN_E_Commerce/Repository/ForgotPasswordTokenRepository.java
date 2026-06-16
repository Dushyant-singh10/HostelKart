package com.example.TTN_E_Commerce.Repository;

import com.example.TTN_E_Commerce.Entity.ForgotPasswordToken;
import com.example.TTN_E_Commerce.Entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface ForgotPasswordTokenRepository extends JpaRepository<ForgotPasswordToken, UUID> {
    Optional<ForgotPasswordToken> findByToken(String token);
    Optional<ForgotPasswordToken> findByUser(User user);
    void deleteByUser(User user);
}
