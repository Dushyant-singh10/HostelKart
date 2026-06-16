package com.example.TTN_E_Commerce.Service.Impl;

import com.example.TTN_E_Commerce.Entity.ActivationToken;
import com.example.TTN_E_Commerce.Entity.User;
import com.example.TTN_E_Commerce.Enum.RoleType;
import com.example.TTN_E_Commerce.Exception.CustomBadRequestException;
import com.example.TTN_E_Commerce.Exception.NotFoundException;
import com.example.TTN_E_Commerce.Repository.ActivationTokenRepository;
import com.example.TTN_E_Commerce.Repository.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class ActivationTokenService {

    @Autowired
    private ActivationTokenRepository repository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private EmailService emailService;

    @Transactional
    public ResponseEntity<?> activate(String token) {
        ActivationToken activationToken = repository.findByToken(token)
                .orElseThrow(() -> new NotFoundException("Token Not Found"));

        if (activationToken.getExpiryDate().isBefore(LocalDateTime.now())) {
            repository.delete(activationToken); // expired token cleanup
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body("Activation token expired. Please request a new one.");
        }

        User user = activationToken.getUser();
        user.setActive(true);
        userRepository.save(user);
        repository.delete(activationToken);
        emailService.sendHtmlMail(user.getEmail(), "Account Verified", "Your account is now active!");
        return new ResponseEntity<>("User Activated", HttpStatus.OK);
    }

    @Transactional
    public ResponseEntity<?> resendToken(String email, RoleType role) {
        // email + role se exact user dhundho — same email pe customer/seller alag hain
        User user = userRepository.findByEmailAndRole(email, role)
                .orElseThrow(() -> new NotFoundException("No account found with this email and role"));

        if (user.isActive()) {
            throw new CustomBadRequestException("Account is already active");
        }

        repository.findByUser(user).ifPresent(token -> {
            repository.delete(token);
            repository.flush();
        });

        String newToken = UUID.randomUUID().toString();
        ActivationToken activationToken = new ActivationToken();
        activationToken.setToken(newToken);
        activationToken.setUser(user);
        activationToken.setExpiryDate(LocalDateTime.now().plusHours(24));
        repository.save(activationToken);

        emailService.sendActivationMail(email, newToken);

        return ResponseEntity.ok("Activation token resent to " + email);
    }
}