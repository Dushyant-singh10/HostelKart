package com.example.TTN_E_Commerce.Service.Impl;

import com.example.TTN_E_Commerce.DTO.OtpVerificationDTO;
import com.example.TTN_E_Commerce.Entity.ActivationToken;
import com.example.TTN_E_Commerce.Entity.User;
import com.example.TTN_E_Commerce.Enum.RoleType;
import com.example.TTN_E_Commerce.Enum.TokenType;
import com.example.TTN_E_Commerce.Exception.CustomBadRequestException;
import com.example.TTN_E_Commerce.Exception.NotFoundException;
import com.example.TTN_E_Commerce.Repository.ActivationTokenRepository;
import com.example.TTN_E_Commerce.Repository.UserRepository;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Map;
import java.util.Optional;

@Service
public class ActivationTokenService {

    @Autowired
    private ActivationTokenRepository repository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private EmailService emailService;
    @Autowired
    @Lazy
    private LoginService loginService;

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
    public ResponseEntity<?> verifySignupOtp(OtpVerificationDTO dto, HttpServletResponse response) {
        User user = userRepository.findByEmailAndRole(dto.getEmail(), dto.getRole())
                .orElseThrow(() -> new NotFoundException("No account found with this email and role"));

        if (user.isActive()) {
            throw new CustomBadRequestException("Account is already active");
        }

        ActivationToken activationToken = repository.findByUserAndTokenType(user, TokenType.SIGNUP)
                .orElseThrow(() -> new CustomBadRequestException("Activation OTP not found or expired. Please request a new one."));

        if (!activationToken.getToken().equals(dto.getOtp())) {
            throw new CustomBadRequestException("Invalid OTP code");
        }

        if (activationToken.getExpiryDate().isBefore(LocalDateTime.now())) {
            repository.delete(activationToken);
            throw new CustomBadRequestException("OTP has expired. Please request a new one.");
        }

        user.setActive(true);
        userRepository.save(user);
        repository.delete(activationToken);

        emailService.sendHtmlMail(user.getEmail(), "Account Verified", "Your account is now active!");

        return loginService.createAuthResponse(user, response);
    }

    @Transactional
    public ResponseEntity<?> resendToken(String email, RoleType role) {
        User user = userRepository.findByEmailAndRole(email, role)
                .orElseThrow(() -> new NotFoundException("No account found with this email and role"));

        if (user.isActive()) {
            throw new CustomBadRequestException("Account is already active");
        }

        Optional<ActivationToken> existingTokenOpt = repository.findByUserAndTokenType(user, TokenType.SIGNUP);
        if (existingTokenOpt.isPresent()) {
            ActivationToken existingToken = existingTokenOpt.get();
            // Rate limiting check (60 seconds)
            if (existingToken.getExpiryDate().isAfter(LocalDateTime.now().plusMinutes(4))) {
                long secondsLeft = ChronoUnit.SECONDS.between(LocalDateTime.now(), existingToken.getExpiryDate().minusMinutes(4));
                throw new CustomBadRequestException("Please wait " + secondsLeft + " seconds before requesting a new OTP.");
            }
            repository.delete(existingToken);
            repository.flush();
        }

        SecureRandom random = new SecureRandom();
        String otp = String.format("%06d", random.nextInt(900000) + 100000);

        ActivationToken activationToken = new ActivationToken();
        activationToken.setToken(otp);
        activationToken.setUser(user);
        activationToken.setExpiryDate(LocalDateTime.now().plusMinutes(5));
        activationToken.setTokenType(TokenType.SIGNUP);
        repository.save(activationToken);
        System.out.println("\n==================================================");
        System.out.println("[OTP CODE] Resent Signup OTP for " + email + " : " + otp);
        System.out.println("==================================================\n");
        emailService.sendActivationOtpMail(email, otp);

        return ResponseEntity.ok(Map.of("message", "Activation token resent to " + email));
    }
}