package com.example.TTN_E_Commerce.Repository;

import com.example.TTN_E_Commerce.Entity.Customer;
import com.example.TTN_E_Commerce.Entity.Seller;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface CustomerRepository extends JpaRepository<Customer, UUID> {
    Optional<Customer> findByUserEmail(String email);
    Optional<Customer> findByUserPassword(String password);
    @Query("SELECT c FROM Customer c WHERE c.user.isDeleted = false " +
            "AND (:email IS NULL OR c.user.email = :email)")
    Page<Customer> findAllActive(@Param("email") String email, Pageable pageable);
}