package com.example.TTN_E_Commerce.Repository;

import com.example.TTN_E_Commerce.Entity.ActivationToken;
import com.example.TTN_E_Commerce.Entity.User;
import com.example.TTN_E_Commerce.Enum.RoleType;
import io.jsonwebtoken.security.Jwks;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID> {
    Optional<User> findByEmail(String email);
    Optional<User> findByRefreshToken(String token);
    boolean existsByEmail(String email);
    @Query("SELECT u FROM User u JOIN u.roles r WHERE u.email = :email AND r.authority = :role")
    Optional<User> findByEmailAndRole(@Param("email") String email, @Param("role") RoleType role);
}