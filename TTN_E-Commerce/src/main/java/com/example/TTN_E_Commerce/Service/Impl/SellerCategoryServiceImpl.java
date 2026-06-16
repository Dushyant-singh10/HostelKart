package com.example.TTN_E_Commerce.Service.Impl;

import com.example.TTN_E_Commerce.DTO.ApiResponse;
import com.example.TTN_E_Commerce.DTO.LeafCategoryResponseDTO;
import com.example.TTN_E_Commerce.Entity.Category;
import com.example.TTN_E_Commerce.Repository.CategoryMetadataFieldValuesRepository;
import com.example.TTN_E_Commerce.Repository.CategoryRepository;
import com.example.TTN_E_Commerce.Service.services.SellerCategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class SellerCategoryServiceImpl implements SellerCategoryService {

    private final CategoryRepository categoryRepository;
    private final CategoryMetadataFieldValuesRepository fieldValuesRepository;

    @Override
    public ResponseEntity<?> listLeafCategories() {

        List<Category> allCategories = categoryRepository.findAll();

        List<LeafCategoryResponseDTO> leafCategories = allCategories.stream()
                .filter(c -> categoryRepository.findByParentCategoryId(c.getId()).isEmpty())
                .map(this::toLeafDTO)
                .toList();

        return ResponseEntity.ok(new ApiResponse("Success", leafCategories));
    }

    private LeafCategoryResponseDTO toLeafDTO(Category category) {

        List<LeafCategoryResponseDTO.ParentDTO> parentChain = new ArrayList<>();
        Category current = category.getParentCategory();
        while (current != null) {
            parentChain.add(0, new LeafCategoryResponseDTO.ParentDTO(current.getId(), current.getName()));
            current = current.getParentCategory();
        }

        List<LeafCategoryResponseDTO.MetadataFieldDTO> fields = fieldValuesRepository
                .findByCategoryId(category.getId())
                .stream()
                .map(fv -> new LeafCategoryResponseDTO.MetadataFieldDTO(
                        fv.getField().getId(),
                        fv.getField().getName(),
                        fv.getFieldValues()))
                .toList();

        return new LeafCategoryResponseDTO(category.getId(), category.getName(), parentChain, fields);
    }
}