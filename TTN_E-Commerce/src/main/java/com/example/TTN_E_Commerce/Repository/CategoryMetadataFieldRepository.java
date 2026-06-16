package com.example.TTN_E_Commerce.Repository;

import com.example.TTN_E_Commerce.Entity.CategoryMetadataField;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CategoryMetadataFieldRepository extends JpaRepository<CategoryMetadataField, UUID> {
    @Override
    Optional<CategoryMetadataField> findById(UUID uuid);
    Optional<CategoryMetadataField> findByNameIgnoreCase(String name);

    @Query("SELECT f FROM CategoryMetadataField f WHERE (:query IS NULL OR LOWER(f.name) LIKE LOWER(CONCAT('%', :query, '%')))")
    Page<CategoryMetadataField> findAllWithFilter(@Param("query") String query, Pageable pageable);
}