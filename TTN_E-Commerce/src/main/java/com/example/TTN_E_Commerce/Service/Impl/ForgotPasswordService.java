package com.example.TTN_E_Commerce.Service.Impl;
import com.example.TTN_E_Commerce.DTO.ResetPasswordDTO;
import com.example.TTN_E_Commerce.Entity.ForgotPasswordToken;
import com.example.TTN_E_Commerce.Entity.User;
import com.example.TTN_E_Commerce.Enum.RoleType;
import com.example.TTN_E_Commerce.Exception.CustomBadRequestException;
import com.example.TTN_E_Commerce.Repository.ForgotPasswordTokenRepository;
import com.example.TTN_E_Commerce.Repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ForgotPasswordService {

    private final UserRepository userRepository;
    private final ForgotPasswordTokenRepository tokenRepository;
    private final EmailService emailService;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public ResponseEntity<?> receiveTokenBasedMail(String email, RoleType role) {
        User user = userRepository.findByEmailAndRole(email, role)
                .orElseThrow(() -> new CustomBadRequestException("No account found with this email and role"));

        if (!user.isActive()) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Account is not activated yet."));
        }

        Optional<ForgotPasswordToken> existingToken = tokenRepository.findByUser(user);
        if (existingToken.isPresent()) {
            ForgotPasswordToken token = existingToken.get();
            if (token.getExpiryDate().isAfter(LocalDateTime.now())) {
                long minutesLeft = ChronoUnit.MINUTES.between(LocalDateTime.now(), token.getExpiryDate());
                long secondsLeft = ChronoUnit.SECONDS.between(LocalDateTime.now(), token.getExpiryDate()) % 60;
                return ResponseEntity.badRequest().body(Map.of(
                        "error", "Reset email already sent.",
                        "message", "Please check your inbox. Token expires in " + minutesLeft + " min " + secondsLeft + " sec.",
                        "minutesRemaining", minutesLeft
                ));
            }
            tokenRepository.delete(token);
            tokenRepository.flush();
        }

        ForgotPasswordToken newToken = new ForgotPasswordToken();
        newToken.setToken(UUID.randomUUID().toString());
        newToken.setExpiryDate(LocalDateTime.now().plusMinutes(15));
        newToken.setUser(user);
        tokenRepository.save(newToken);

        emailService.resetPassword(email, newToken.getToken());

        return ResponseEntity.ok(Map.of("message", "Password reset email sent. Check your inbox."+newToken.getToken()));
    }

    @Transactional
    public ResponseEntity<?> changePassword(ResetPasswordDTO dto, String token) {
        ForgotPasswordToken resetToken = tokenRepository.findByToken(token)
                .orElseThrow(() -> new CustomBadRequestException("Invalid or expired reset token"));

        if (resetToken.getExpiryDate().isBefore(LocalDateTime.now())) {
            tokenRepository.delete(resetToken);
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Reset token has expired. Please request a new one."));
        }

        if (!dto.getPassword().equals(dto.getConfirmPassword())) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Password and Confirm Password do not match."));
        }

        User user = resetToken.getUser();

        if (passwordEncoder.matches(dto.getPassword(), user.getPassword())) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "New password cannot be the same as old password."));
        }

        user.setPassword(passwordEncoder.encode(dto.getPassword()));
        user.setLocked(false);
        user.setInvalidAttemptCount(0);
        user.setPasswordUpdateDate(LocalDateTime.now());
        userRepository.save(user);
        tokenRepository.delete(resetToken);

        return ResponseEntity.ok(Map.of("message", "Password changed successfully! You can now login."));
    }
}