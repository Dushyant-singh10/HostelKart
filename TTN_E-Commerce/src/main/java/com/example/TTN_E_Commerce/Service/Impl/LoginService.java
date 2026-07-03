package com.example.TTN_E_Commerce.Service.Impl;

import com.example.TTN_E_Commerce.DTO.LoginDTO;
import com.example.TTN_E_Commerce.DTO.OtpVerificationDTO;
import com.example.TTN_E_Commerce.Entity.ActivationToken;
import com.example.TTN_E_Commerce.Entity.User;
import com.example.TTN_E_Commerce.Enum.RoleType;
import com.example.TTN_E_Commerce.Enum.TokenType;
import com.example.TTN_E_Commerce.Exception.CustomBadRequestException;
import com.example.TTN_E_Commerce.Exception.NotFoundException;
import com.example.TTN_E_Commerce.Repository.ActivationTokenRepository;
import com.example.TTN_E_Commerce.Repository.UserRepository;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Map;
import java.util.Optional;

@Service
public class LoginService {

    @Autowired
    private EmailService emailService;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private JwtService jwtService;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private ActivationTokenRepository activationTokenRepository;

    @Transactional
    public ResponseEntity<?> login(LoginDTO dto, RoleType role, HttpServletResponse response)
    {
        User user = userRepository.findByEmailAndRole(dto.getEmail(), role)
                .orElseThrow(() -> new CustomBadRequestException("Invalid email or password"));
        if (!user.isActive()) {
            throw new CustomBadRequestException("Account not activated");
        }
        if (user.isLocked()) {
            throw new CustomBadRequestException("Account locked");
        }

        if (!passwordEncoder.matches(dto.getPassword(), user.getPassword())) {
            int attempts = (user.getInvalidAttemptCount() == null ? 0 : user.getInvalidAttemptCount()) + 1;
            user.setInvalidAttemptCount(attempts);

            if(attempts >= 3){
                user.setLocked(true);
                userRepository.save(user);
                emailService.sendHtmlMail(
                        user.getEmail(),
                        "Account Locked",
                        "Your account has been locked because of 3 failed login attempts"
                );
                throw new CustomBadRequestException("Account locked");
            }
            userRepository.save(user);
            throw new CustomBadRequestException("Invalid password. Attempts: " + attempts);
        }

        // Credentials are valid, clear attempt counter
        user.setInvalidAttemptCount(0);
        userRepository.save(user);

        // Generate Login OTP instead of logging the user in immediately
        activationTokenRepository.findByUserAndTokenType(user, TokenType.LOGIN).ifPresent(token -> {
            activationTokenRepository.delete(token);
            activationTokenRepository.flush();
        });

        SecureRandom random = new SecureRandom();
        String otp = String.format("%06d", random.nextInt(900000) + 100000);

        ActivationToken loginToken = new ActivationToken();
        loginToken.setToken(otp);
        loginToken.setUser(user);
        loginToken.setExpiryDate(LocalDateTime.now().plusMinutes(5));
        loginToken.setTokenType(TokenType.LOGIN);
        activationTokenRepository.save(loginToken);
        System.out.println("\n==================================================");
        System.out.println("[OTP CODE] Login OTP for " + user.getEmail() + " : " + otp);
        System.out.println("==================================================\n");
        emailService.sendLoginOtpMail(user.getEmail(), otp);

        return ResponseEntity.ok(Map.of(
                "otpRequired", true,
                "email", user.getEmail(),
                "role", role.name(),
                "message", "Login OTP code sent to your registered email."
        ));
    }

    @Transactional
    public ResponseEntity<?> verifyLoginOtp(OtpVerificationDTO dto, HttpServletResponse response) {
        User user = userRepository.findByEmailAndRole(dto.getEmail(), dto.getRole())
                .orElseThrow(() -> new NotFoundException("No account found with this email and role"));

        if (!user.isActive()) {
            throw new CustomBadRequestException("Account not activated");
        }
        if (user.isLocked()) {
            throw new CustomBadRequestException("Account locked");
        }

        ActivationToken loginToken = activationTokenRepository.findByUserAndTokenType(user, TokenType.LOGIN)
                .orElseThrow(() -> new CustomBadRequestException("Login OTP not found or expired. Please login again."));

        if (!loginToken.getToken().equals(dto.getOtp())) {
            throw new CustomBadRequestException("Invalid OTP code");
        }

        if (loginToken.getExpiryDate().isBefore(LocalDateTime.now())) {
            activationTokenRepository.delete(loginToken);
            throw new CustomBadRequestException("OTP has expired. Please login again.");
        }

        // Delete OTP (single-use)
        activationTokenRepository.delete(loginToken);

        // Clear invalid login count
        user.setInvalidAttemptCount(0);
        userRepository.save(user);

        return createAuthResponse(user, response);
    }

    @Transactional
    public ResponseEntity<?> resendLoginOtp(String email, RoleType role) {
        User user = userRepository.findByEmailAndRole(email, role)
                .orElseThrow(() -> new NotFoundException("No account found with this email and role"));

        if (!user.isActive()) {
            throw new CustomBadRequestException("Account not activated");
        }
        if (user.isLocked()) {
            throw new CustomBadRequestException("Account locked");
        }

        Optional<ActivationToken> existingTokenOpt = activationTokenRepository.findByUserAndTokenType(user, TokenType.LOGIN);
        if (existingTokenOpt.isPresent()) {
            ActivationToken existingToken = existingTokenOpt.get();
            // Rate limit (60 seconds)
            if (existingToken.getExpiryDate().isAfter(LocalDateTime.now().plusMinutes(4))) {
                long secondsLeft = ChronoUnit.SECONDS.between(LocalDateTime.now(), existingToken.getExpiryDate().minusMinutes(4));
                throw new CustomBadRequestException("Please wait " + secondsLeft + " seconds before requesting a new OTP.");
            }
            activationTokenRepository.delete(existingToken);
            activationTokenRepository.flush();
        }

        SecureRandom random = new SecureRandom();
        String otp = String.format("%06d", random.nextInt(900000) + 100000);

        ActivationToken loginToken = new ActivationToken();
        loginToken.setToken(otp);
        loginToken.setUser(user);
        loginToken.setExpiryDate(LocalDateTime.now().plusMinutes(5));
        loginToken.setTokenType(TokenType.LOGIN);
        activationTokenRepository.save(loginToken);
        System.out.println("\n==================================================");
        System.out.println("[OTP CODE] Resent Login OTP for " + email + " : " + otp);
        System.out.println("==================================================\n");
        emailService.sendLoginOtpMail(email, otp);

        return ResponseEntity.ok(Map.of("message", "Login OTP resent to " + email));
    }

    public ResponseEntity<?> createAuthResponse(User user, HttpServletResponse response) {
        String accessToken = jwtService.generateAccessToken(user);
        String refreshToken = jwtService.generateRefreshToken(user);
        user.setRefreshToken(refreshToken);
        userRepository.save(user);

        Cookie cookie = new Cookie("refreshToken", refreshToken);
        cookie.setHttpOnly(true);
        cookie.setPath("/");
        cookie.setMaxAge(24 * 60 * 60);
        response.addCookie(cookie);

        return ResponseEntity.ok(Map.of(
                "accessToken", accessToken,
                "email", user.getEmail(),
                "role", user.getRoles().iterator().next().getAuthority().name()
        ));
    }

    public ResponseEntity<?> refreshAccessToken(HttpServletRequest request)
    {
        String refreshToken = null;
        if (request.getCookies() != null) {
            for (Cookie cookie : request.getCookies()) {
                if ("refreshToken".equals(cookie.getName())) {
                    refreshToken = cookie.getValue();
                    break;
                }
            }
        }

        if (refreshToken == null) {
            throw new CustomBadRequestException("No refresh token found. Please login again.");
        }

        jwtService.validateRefreshToken(refreshToken); // expire check

        User user = userRepository.findByRefreshToken(refreshToken)
                .orElseThrow(() -> new NotFoundException("Invalid or expired refresh token"));

        if (user.isLocked()) throw new CustomBadRequestException("Account is locked");
        if (!user.isActive()) throw new CustomBadRequestException("Account is not activated");

        String newAccessToken = jwtService.generateAccessToken(user);
        return ResponseEntity.ok(Map.of("accessToken", newAccessToken));
    }

    public ResponseEntity<?> logOut(HttpServletResponse response)
    {
        String email = (String) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String roleName = SecurityContextHolder.getContext().getAuthentication()
                .getAuthorities().iterator().next()
                .getAuthority().replace("ROLE_", "");
        RoleType roleType = RoleType.valueOf(roleName);

        User user = userRepository.findByEmailAndRole(email, roleType)
                .orElseThrow(() -> new NotFoundException("User not found"));

        if(user.getRefreshToken() == null) {
            throw new CustomBadRequestException("Already Logged Out");
        }

        user.setRefreshToken(null);
        userRepository.save(user);

        Cookie cookie = new Cookie("refreshToken", "");
        cookie.setHttpOnly(true);
        cookie.setPath("/");
        cookie.setMaxAge(0);
        response.addCookie(cookie);

        return ResponseEntity.ok("Logged out successfully");
    }

}