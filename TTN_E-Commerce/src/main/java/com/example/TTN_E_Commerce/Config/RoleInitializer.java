package com.example.TTN_E_Commerce.Config;

import com.example.TTN_E_Commerce.Entity.Role;
import com.example.TTN_E_Commerce.Enum.RoleType;
import com.example.TTN_E_Commerce.Repository.RoleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@Order(1)
@RequiredArgsConstructor
public class RoleInitializer implements CommandLineRunner {

    private final RoleRepository roleRepository;

    @Override
    public void run(String... args) {
        for (RoleType roleType : RoleType.values()) {
            if (roleRepository.findByAuthority(roleType).isEmpty()) {
                Role role = new Role();
                role.setAuthority(roleType);
                roleRepository.save(role);
                System.out.println("[RoleInitializer] Role created: " + roleType);
            }
        }
    }
}
