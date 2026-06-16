package com.example.TTN_E_Commerce.Controller;

import com.example.TTN_E_Commerce.DTO.CategoryDTO;
import com.example.TTN_E_Commerce.DTO.CategoryMetadataFieldsRequestDTO;
import com.example.TTN_E_Commerce.DTO.CategoryUpdateDTO;
import com.example.TTN_E_Commerce.Service.services.CategoryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/admin/categories")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryService categoryService;

    @PostMapping
    public ResponseEntity<?> addCategory(@Valid @RequestBody CategoryDTO dto) {
        return categoryService.addCategory(dto);
    }

    @GetMapping
    public ResponseEntity<?> getAllCategories(
            @RequestParam(defaultValue = "10") int max,
            @RequestParam(defaultValue = "0") int offset,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "asc") String order,
            @RequestParam(required = false) String query,
            @RequestParam(required = false) UUID categoryId) {
        return categoryService.getAllCategories(max, offset, sortBy, order, query, categoryId);
    }
    @PutMapping
    public ResponseEntity<?> updateCategory(@Valid @RequestBody CategoryUpdateDTO dto) {
        return categoryService.updateCategory(dto);
    }

    @PostMapping("/metadata-fields")
    public ResponseEntity<?> addMetadataFields(
            @Valid @RequestBody CategoryMetadataFieldsRequestDTO dto) {
        return categoryService.addMetadataFieldsToCategory(dto);
    }
}