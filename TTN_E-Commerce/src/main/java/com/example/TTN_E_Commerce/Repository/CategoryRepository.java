package com.example.TTN_E_Commerce.Repository;

import com.example.TTN_E_Commerce.Entity.Category;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface CategoryRepository extends JpaRepository<Category, UUID> {
    Optional<Category> findByNameIgnoreCaseAndParentCategoryIsNull(String name);
    Optional<Category> findByNameIgnoreCaseAndParentCategory_Id(String name, UUID parentId);
    @Query("SELECT COUNT(p) > 0 FROM Product p WHERE p.category.id = :categoryId AND p.isDeleted = false")
    boolean hasActiveProducts(@Param("categoryId") UUID categoryId);

    @Query("SELECT c FROM Category c WHERE (:query IS NULL OR LOWER(c.name) LIKE LOWER(CONCAT('%', :query, '%'))) AND (:categoryId IS NULL OR c.id = :categoryId)")
    Page<Category> findAllWithFilter(
            @Param("query") String query,
            @Param("categoryId") UUID categoryId,
            Pageable pageable);

    List<Category> findByParentCategoryId(UUID parentId);

    Optional<Category> findById(UUID id);
}