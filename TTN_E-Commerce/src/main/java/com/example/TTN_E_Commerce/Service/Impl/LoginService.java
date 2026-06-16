package com.example.TTN_E_Commerce.Service.Impl;

import com.example.TTN_E_Commerce.DTO.LoginDTO;
import com.example.TTN_E_Commerce.Entity.User;
import com.example.TTN_E_Commerce.Enum.RoleType;
import com.example.TTN_E_Commerce.Exception.CustomBadRequestException;
import com.example.TTN_E_Commerce.Exception.NotFoundException;
import com.example.TTN_E_Commerce.Repository.UserRepository;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Map;

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

        user.setInvalidAttemptCount(0);
        String accessToken = jwtService.generateAccessToken(user);
        String refreshToken = jwtService.generateRefreshToken(user);
        user.setRefreshToken(refreshToken);
        userRepository.save(user);

        Cookie cookie = new Cookie("refreshToken", refreshToken);
        cookie.setHttpOnly(true);
        cookie.setPath("/");
        cookie.setMaxAge(24 * 60 * 60);
        response.addCookie(cookie);

        return ResponseEntity.ok(Map.of("accessToken", accessToken));
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