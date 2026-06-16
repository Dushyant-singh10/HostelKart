package com.example.TTN_E_Commerce.Service.Impl;

import com.example.TTN_E_Commerce.DTO.ApiResponse;
import com.example.TTN_E_Commerce.DTO.CategoryFilterDetailsDTO;
import com.example.TTN_E_Commerce.Entity.Category;
import com.example.TTN_E_Commerce.Exception.CustomBadRequestException;
import com.example.TTN_E_Commerce.Exception.NotFoundException;
import com.example.TTN_E_Commerce.Repository.CategoryMetadataFieldValuesRepository;
import com.example.TTN_E_Commerce.Repository.CategoryRepository;
import com.example.TTN_E_Commerce.Repository.ProductRepository;
import com.example.TTN_E_Commerce.Service.services.CustomerCategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@RequiredArgsConstructor
public class CustomerCategoryServiceImpl implements CustomerCategoryService {

    private final CategoryRepository categoryRepository;
    private final CategoryMetadataFieldValuesRepository fieldValuesRepository;
    private final ProductRepository productRepository;

    @Override
    public ResponseEntity<?> listCategories(UUID categoryId) {

        List<Category> result;

        if (categoryId == null) {
            result = categoryRepository.findAll().stream()
                    .filter(c -> c.getParentCategory() == null)
                    .toList();
        } else {
            categoryRepository.findById(categoryId)
                    .orElseThrow(() -> new NotFoundException("Category not found with id: " + categoryId));

            result = categoryRepository.findByParentCategoryId(categoryId);
        }

        List<Map<String, Object>> data = result.stream()
                .map(c -> {
            Map<String, Object> map = new HashMap<>();
            map.put("id", c.getId());
            map.put("name", c.getName());
            return map;
        }).toList();

        return ResponseEntity.ok(new ApiResponse("Success", data));
    }

    @Override
    public ResponseEntity<?> getCategoryFilterDetails(UUID categoryId) {

        if (categoryId == null) {
            throw new CustomBadRequestException("Category ID is required");
        }

        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new NotFoundException("Category not found with id: " + categoryId));

        List<UUID> categoryIds = new ArrayList<>();
        collectAllDescendantIds(categoryId, categoryIds);
        categoryIds.add(categoryId);

        List<CategoryFilterDetailsDTO.MetadataFieldDTO> fields = fieldValuesRepository.findByCategoryId(categoryId)
                .stream()
                .map(fv -> new CategoryFilterDetailsDTO.MetadataFieldDTO(
                        fv.getField().getId(),
                        fv.getField().getName(),
                        fv.getFieldValues()))
                .toList();

        List<String> brands = productRepository.findDistinctBrandsByCategoryIds(categoryIds);

        Double minPrice = productRepository.findMinPriceByCategoryIds(categoryIds);
        Double maxPrice = productRepository.findMaxPriceByCategoryIds(categoryIds);

        CategoryFilterDetailsDTO dto = new CategoryFilterDetailsDTO(
                category.getId(),
                category.getName(),
                fields,
                brands,
                minPrice,
                maxPrice);

        return ResponseEntity.ok(new ApiResponse("Success", dto));
    }
    private void collectAllDescendantIds(UUID categoryId, List<UUID> ids) {
        List<Category> children = categoryRepository.findByParentCategoryId(categoryId);
        for (Category child : children) {
            ids.add(child.getId());
            collectAllDescendantIds(child.getId(), ids);
        }
    }
}