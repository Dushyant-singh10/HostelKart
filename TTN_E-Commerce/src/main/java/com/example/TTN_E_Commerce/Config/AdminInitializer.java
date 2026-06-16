package com.example.TTN_E_Commerce.Config;

import com.example.TTN_E_Commerce.Entity.Role;
import com.example.TTN_E_Commerce.Entity.User;
import com.example.TTN_E_Commerce.Enum.RoleType;
import com.example.TTN_E_Commerce.Exception.CustomBadRequestException;
import com.example.TTN_E_Commerce.Repository.RoleRepository;
import com.example.TTN_E_Commerce.Repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Set;

@Component
@Order(2)
@RequiredArgsConstructor
public class AdminInitializer implements CommandLineRunner {

    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;

    @Value("${app.admin.email}")
    private String email;

    @Value("${app.admin.password}")
    private String password;

    @Value("${app.admin.firstName}")
    private String firstName;

    @Value("${app.admin.middleName}")
    private String middleName;

    @Value("${app.admin.lastName}")
    private String lastName;

    @Override
    public void run(String... args) {

        if (userRepository.existsByEmail(email)) return;

        Role adminRole = roleRepository.findByAuthority(RoleType.ADMIN)
                .orElseThrow(() -> new CustomBadRequestException("ADMIN role not found!"));

        User user = new User();
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(password));
        user.setFirstName(firstName);
        user.setMiddleName(middleName);
        user.setLastName(lastName);
        user.setRoles(Set.of(adminRole));
        user.setActive(true);
        user.setLocked(false);
        user.setDeleted(false);
        user.setExpired(false);
        user.setInvalidAttemptCount(0);
        user.setPasswordUpdateDate(LocalDateTime.now());

        userRepository.save(user);
    }
}