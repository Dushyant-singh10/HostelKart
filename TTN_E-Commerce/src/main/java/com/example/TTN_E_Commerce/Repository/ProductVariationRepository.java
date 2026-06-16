package com.example.TTN_E_Commerce.Repository;

import com.example.TTN_E_Commerce.Entity.ProductVariation;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface ProductVariationRepository extends JpaRepository<ProductVariation, UUID> {

    @Query("SELECT pv FROM ProductVariation pv WHERE pv.product.id = :productId")
    Page<ProductVariation> findByProductId(@Param("productId") UUID productId, Pageable pageable);

    @Query("SELECT pv FROM ProductVariation pv WHERE pv.product.id = :productId")
    List<ProductVariation> findAllByProductId(@Param("productId") UUID productId);

    @Query("SELECT pv FROM ProductVariation pv WHERE pv.product.id = :productId AND (:query IS NULL OR LOWER(pv.metadata) LIKE %:query%)")
    Page<ProductVariation> findByProductIdWithFilter(@Param("productId") UUID productId, @Param("query") String query, Pageable pageable);

    @Query("SELECT pv FROM ProductVariation pv WHERE pv.product.id = :productId AND pv.isActive = true")
    List<ProductVariation> findActiveVariationsByProductId(@Param("productId") UUID productId);

    @Query("SELECT COUNT(pv) > 0 FROM ProductVariation pv WHERE pv.product.id = :productId AND pv.isActive = true")
    boolean hasActiveVariations(@Param("productId") UUID productId);

}