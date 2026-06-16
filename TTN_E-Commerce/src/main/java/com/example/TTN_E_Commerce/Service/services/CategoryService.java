package com.example.TTN_E_Commerce.Service.services;

import com.example.TTN_E_Commerce.DTO.CategoryDTO;
import com.example.TTN_E_Commerce.DTO.CategoryMetadataFieldsRequestDTO;
import com.example.TTN_E_Commerce.DTO.CategoryUpdateDTO;
import org.springframework.http.ResponseEntity;

import java.util.UUID;

public interface CategoryService {

    ResponseEntity<?> addCategory(CategoryDTO dto);

    ResponseEntity<?> getAllCategories(int max, int offset, String sortBy, String order, String query, UUID categoryId);

    ResponseEntity<?> updateCategory(CategoryUpdateDTO dto);

    ResponseEntity<?> addMetadataFieldsToCategory(CategoryMetadataFieldsRequestDTO dto);

}