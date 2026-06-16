package com.example.TTN_E_Commerce.Repository;

import com.example.TTN_E_Commerce.Entity.Seller;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface SellerRepository extends JpaRepository<Seller, UUID> {
    Optional<Seller> findByUserEmail(String email);
    boolean existsByGst(String gst);
    boolean existsByCompanyContact(String companyContact);
    boolean existsByCompanyName(String companyName);

    @Query("SELECT s FROM Seller s WHERE s.user.isDeleted = false " +
            "AND (:email IS NULL OR s.user.email = :email)")
    Page<Seller> findAllActive(@Param("email") String email, Pageable pageable);
}