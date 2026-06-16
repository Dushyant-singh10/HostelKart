package com.example.TTN_E_Commerce.Repository;

import com.example.TTN_E_Commerce.Entity.Role;
import com.example.TTN_E_Commerce.Enum.RoleType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface RoleRepository extends JpaRepository<Role, UUID> {
    Optional<Role> findByAuthority(RoleType roleType);
}