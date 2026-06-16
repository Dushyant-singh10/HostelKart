package com.example.TTN_E_Commerce.Repository;

import com.example.TTN_E_Commerce.Entity.ActivationToken;
import com.example.TTN_E_Commerce.Entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface ActivationTokenRepository extends JpaRepository<ActivationToken, UUID> {
    Optional<ActivationToken> findByToken(String token);
    Optional<ActivationToken> findByUser(User user);
}
