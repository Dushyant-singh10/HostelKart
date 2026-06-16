package com.example.TTN_E_Commerce.Repository;

import com.example.TTN_E_Commerce.Entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ProductRepository extends JpaRepository<Product, UUID> {

    Optional<Product> findByNameIgnoreCaseAndBrandIgnoreCaseAndSeller_UserId(
            String name, String brand, UUID sellerId);


    @Query("SELECT p FROM Product p WHERE p.seller.userId = :sellerId AND p.isDeleted = false")
    List<Product> findActiveProductsBySeller(@Param("sellerId") UUID sellerId);

    @Query("SELECT p FROM Product p WHERE p.seller.userId = :sellerId AND p.isDeleted = false AND (:query IS NULL OR LOWER(p.name) LIKE %:query% OR LOWER(p.brand) LIKE %:query%)")
    Page<Product> findActiveProductsBySellerWithFilter(@Param("sellerId") UUID sellerId, @Param("query") String query, Pageable pageable);

    @Query("SELECT p FROM Product p WHERE p.id = :id AND p.isDeleted = false AND p.isActive = true")
    Optional<Product> findActiveProductById(@Param("id") UUID id);

    @Query("SELECT p FROM Product p WHERE p.category.id IN :categoryIds AND p.isDeleted = false AND p.isActive = true")
    Page<Product> findActiveProductsByCategoryIds(@Param("categoryIds") List<UUID> categoryIds, Pageable pageable);

    @Query("SELECT p FROM Product p WHERE p.category.id = :categoryId AND p.isDeleted = false AND p.isActive = true AND p.id != :excludeProductId")
    Page<Product> findSimilarProducts(@Param("categoryId") UUID categoryId, @Param("excludeProductId") UUID excludeProductId, Pageable pageable);

    @Query("SELECT p FROM Product p WHERE p.isDeleted = false AND p.isActive = true AND (:sellerId   IS NULL OR p.seller.userId = :sellerId) " +
            "AND (:categoryId IS NULL OR p.category.id  = :categoryId)")
    Page<Product> findAllActiveProductsWithFilter(@Param("sellerId")   UUID sellerId, @Param("categoryId") UUID categoryId, Pageable pageable);

    @Query("SELECT p FROM Product p WHERE p.id = :productId AND p.isDeleted = false AND (:sellerId   IS NULL OR p.seller.userId = :sellerId) " +
            "AND (:categoryId IS NULL OR p.category.id  = :categoryId)")
    Page<Product> findProductByIdWithFilters(@Param("productId")  UUID productId, @Param("sellerId")   UUID sellerId, @Param("categoryId") UUID categoryId, Pageable pageable);

    @Query("SELECT DISTINCT p.brand FROM Product p WHERE p.category.id IN :categoryIds AND p.isDeleted = false AND p.isActive = true AND p.brand IS NOT NULL")
    List<String> findDistinctBrandsByCategoryIds(@Param("categoryIds") List<UUID> categoryIds);

    @Query("SELECT MIN(pv.price) FROM ProductVariation pv WHERE pv.product.category.id IN :categoryIds " +
            "AND pv.product.isDeleted = false AND pv.product.isActive = true AND pv.isActive = true")
    Double findMinPriceByCategoryIds(@Param("categoryIds") List<UUID> categoryIds);

    @Query("SELECT MAX(pv.price) FROM ProductVariation pv WHERE pv.product.category.id IN :categoryIds " +
            "AND pv.product.isDeleted = false AND pv.product.isActive = true AND pv.isActive = true")
    Double findMaxPriceByCategoryIds(@Param("categoryIds") List<UUID> categoryIds);
}