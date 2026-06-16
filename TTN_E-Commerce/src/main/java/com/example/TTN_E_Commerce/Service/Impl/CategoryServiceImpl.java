package com.example.TTN_E_Commerce.Service.Impl;

import com.example.TTN_E_Commerce.DTO.*;
import com.example.TTN_E_Commerce.Entity.Category;
import com.example.TTN_E_Commerce.Entity.CategoryMetadataField;
import com.example.TTN_E_Commerce.Entity.CategoryMetadataFieldValues;
import com.example.TTN_E_Commerce.Exception.CustomBadRequestException;
import com.example.TTN_E_Commerce.Exception.NotFoundException;
import com.example.TTN_E_Commerce.Repository.CategoryMetadataFieldRepository;
import com.example.TTN_E_Commerce.Repository.CategoryMetadataFieldValuesRepository;
import com.example.TTN_E_Commerce.Repository.CategoryRepository;
import com.example.TTN_E_Commerce.Service.services.CategoryService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;
    private final CategoryMetadataFieldValuesRepository categoryMetadataFieldValuesRepository;
    private final CategoryMetadataFieldRepository categoryMetadataFieldRepository;
    @Override
    @Transactional
    public ResponseEntity<?> addCategory(CategoryDTO dto) {
        String name= dto.getName().trim();

        if(dto.getParentCategory()== null)
        {
            if (categoryRepository.findByNameIgnoreCaseAndParentCategoryIsNull(name).isPresent()) {
                throw new CustomBadRequestException("Category with name '" + name + "' already exists at root level");
            }
            Category category= new Category();
            category.setName(dto.getName());
            categoryRepository.save(category);

            return ResponseEntity.ok(new ApiResponse("Category created successfully",
                    Map.of("id", category.getId(), "name", category.getName())));
        }
        Category parent = categoryRepository.findById(dto.getParentCategory())
                .orElseThrow(() -> new NotFoundException("Parent category not found with id: " + dto.getParentCategory()));
        if (categoryRepository.hasActiveProducts(parent.getId())) {
            throw new CustomBadRequestException("Parent category is already associated with products. Cannot add sub-category to it");
        }

        if (isNameExistsInAncestorChain(name, parent)) {
            throw new CustomBadRequestException(
                    "Category with name '" + name + "' already exists in this category's ancestor chain");
        }
        if (isNameExistsInDescendants(name,parent.getId())) {
            throw new CustomBadRequestException(
                    "Category with name '" + name + "' already exists in this category's Children chain");
        }


        if (categoryRepository.findByNameIgnoreCaseAndParentCategory_Id(name, dto.getParentCategory()).isPresent()) {
            throw new CustomBadRequestException(
                    "Category with name '" + name + "' already exists under this parent");
        }

        Category category = new Category();
        category.setName(name);
        category.setParentCategory(parent);
        categoryRepository.save(category);

        return ResponseEntity.ok(new ApiResponse("Category created successfully",
                Map.of("id", category.getId(),
                        "name", category.getName(),
                        "parentId", parent.getId(),
                        "parentName", parent.getName())));
    }
    private boolean isNameExistsInAncestorChain(String name, Category category) {
        Category current = category;
        while (current != null) {
            if (current.getName().equalsIgnoreCase(name)) {
                return true;
            }
            current = current.getParentCategory();
        }
        return false;
    }



    @Override
    public ResponseEntity<?> getAllCategories(int max, int offset, String sortBy, String order, String query, UUID categoryId) {
        if (!List.of("id", "name").contains(sortBy)) {
            throw new CustomBadRequestException("Invalid sort field. Allowed: id, name");
        }

        if (categoryId != null) {
            categoryRepository.findById(categoryId)
                    .orElseThrow(() -> new NotFoundException("Category not found with id: " + categoryId));
        }

        Sort sort = order.equalsIgnoreCase("desc")
                ? Sort.by(sortBy).descending()
                : Sort.by(sortBy).ascending();

        Page<Category> page = categoryRepository.findAllWithFilter(
                (query == null || query.isBlank()) ? null : query, categoryId, PageRequest.of(offset, max, sort));

        List<CategoryResponseDTO> categories = page.getContent()
                .stream()
                .map(this::toResponseDTO)
                .toList();

        Map<String, Object> data = new HashMap<>();
        data.put("totalElements", page.getTotalElements());
        data.put("totalPages", page.getTotalPages());
        data.put("currentPage", offset);
        data.put("categories", categories);

        return ResponseEntity.ok(new ApiResponse("Success", data));
    }

    private CategoryResponseDTO toResponseDTO(Category category) {

        List<CategoryResponseDTO.ParentDTO> parentChain = new ArrayList<>();
        Category current = category.getParentCategory();
        while (current != null) {
            parentChain.add(0, new CategoryResponseDTO.ParentDTO(current.getId(), current.getName()));
            current = current.getParentCategory();
        }

        List<CategoryResponseDTO.ChildDTO> children = categoryRepository
                .findByParentCategoryId(category.getId())
                .stream()
                .map(c -> new CategoryResponseDTO.ChildDTO(c.getId(), c.getName()))
                .toList();

        List<CategoryResponseDTO.FieldValueDTO> fields = categoryMetadataFieldValuesRepository
                .findByCategoryId(category.getId())
                .stream()
                .map(fv -> new CategoryResponseDTO.FieldValueDTO(
                        fv.getField().getId(),
                        fv.getField().getName(),
                        fv.getFieldValues()))
                .toList();

        return new CategoryResponseDTO(category.getId(), category.getName(), parentChain, children, fields);
    }
    @Transactional
    @Override
    public ResponseEntity<?> updateCategory(CategoryUpdateDTO dto) {
        Category category = categoryRepository.findById(dto.getId())
                .orElseThrow(() -> new NotFoundException("Category not found with this id"));

        String name = dto.getName().trim();

        if (category.getName().equalsIgnoreCase(name)) {
            return ResponseEntity.ok(new ApiResponse("Category updated successfully"));
        }

        categoryRepository.findByNameIgnoreCaseAndParentCategoryIsNull(name)
                .filter(c -> !c.getId().equals(dto.getId()))
                .ifPresent(c -> { throw new CustomBadRequestException(
                        "Category name '" + name + "' already exists at root level"); });

        if (category.getParentCategory() != null) {
            categoryRepository.findByNameIgnoreCaseAndParentCategory_Id(
                            name, category.getParentCategory().getId())
                    .filter(c -> !c.getId().equals(dto.getId()))
                    .ifPresent(c -> { throw new CustomBadRequestException(
                            "Category name '" + name + "' already exists under same parent"); });
        }

        if (isNameExistsInAncestorChain(name, category.getParentCategory())) {
            throw new CustomBadRequestException(
                    "Category name '" + name + "' already exists in ancestor chain");
        }

        if (isNameExistsInDescendants(name, category.getId())) {
            throw new CustomBadRequestException(
                    "Category name '" + name + "' already exists in sub-categories");
        }

        category.setName(name);
        categoryRepository.save(category);

        Map<String, Object> data = new HashMap<>();
        data.put("id", category.getId());
        data.put("name", category.getName());
        if (category.getParentCategory() != null) {
            data.put("parentId", category.getParentCategory().getId());
        }
        return ResponseEntity.ok(new ApiResponse("Category updated successfully", data));
    }

    private boolean isNameExistsInDescendants(String name, UUID categoryId) {
        List<Category> children = categoryRepository.findByParentCategoryId(categoryId);
        for (Category child : children) {
            if (child.getName().equalsIgnoreCase(name)) return true;
            if (isNameExistsInDescendants(name, child.getId())) return true;
        }
        return false;
    }

    @Override
    public ResponseEntity<?> addMetadataFieldsToCategory(CategoryMetadataFieldsRequestDTO dto) {

        Category category = categoryRepository.findById(dto.getCategoryId())
                .orElseThrow(() -> new NotFoundException("Category not found with id: " + dto.getCategoryId()));

        List<Map<String, Object>> savedFields = new ArrayList<>();

        for (CategoryMetadataFieldsRequestDTO.FieldEntry entry : dto.getFields()) {

            CategoryMetadataField field = categoryMetadataFieldRepository.findById(entry.getFieldId())
                    .orElseThrow(() -> new NotFoundException(
                            "Metadata field not found with id: " + entry.getFieldId()));

            if (entry.getValues().isEmpty()) {
                throw new CustomBadRequestException(
                        "At least one value is required for field: " + field.getName());
            }

            List<String> trimmedValues = entry.getValues().stream()
                    .map(String::trim)
                    .filter(v -> !v.isEmpty())
                    .toList();

            long distinctCount = trimmedValues.stream()
                    .map(String::toLowerCase)
                    .distinct()
                    .count();

            if (distinctCount != trimmedValues.size()) {
                throw new CustomBadRequestException(
                        "Duplicate values found for field: " + field.getName() + ". Values must be unique");
            }

            CategoryMetadataFieldValues fieldValues = categoryMetadataFieldValuesRepository
                    .findByCategoryIdAndFieldId(category.getId(), field.getId())
                    .orElse(new CategoryMetadataFieldValues());

            fieldValues.setCategory(category);
            fieldValues.setField(field);
            fieldValues.setFieldValues(String.join(",", trimmedValues));  // store as CSV
            categoryMetadataFieldValuesRepository.save(fieldValues);

            savedFields.add(Map.of(
                    "fieldId", field.getId(),
                    "fieldName", field.getName(),
                    "values", trimmedValues));
        }

        return ResponseEntity.ok(new ApiResponse("Metadata fields added to category successfully",
                Map.of("categoryId", category.getId(),
                        "categoryName", category.getName(),
                        "fields", savedFields)));
    }

}