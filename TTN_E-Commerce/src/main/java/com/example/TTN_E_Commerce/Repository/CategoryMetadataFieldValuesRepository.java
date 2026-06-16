package com.example.TTN_E_Commerce.Repository;

import com.example.TTN_E_Commerce.Entity.CategoryMetadataFieldValues;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CategoryMetadataFieldValuesRepository extends JpaRepository<CategoryMetadataFieldValues, UUID> {

    @Query("SELECT v FROM CategoryMetadataFieldValues v WHERE v.category.id = :categoryId")
    List<CategoryMetadataFieldValues> findByCategoryId(@Param("categoryId") UUID categoryId);

    @Query("SELECT v FROM CategoryMetadataFieldValues v WHERE v.category.id = :categoryId AND v.field.id = :fieldId")
    Optional<CategoryMetadataFieldValues> findByCategoryIdAndFieldId(@Param("categoryId") UUID categoryId, @Param("fieldId") UUID fieldId);
}