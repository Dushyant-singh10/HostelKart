package com.example.TTN_E_Commerce.Service.Impl;

import com.example.TTN_E_Commerce.DTO.MetadataFieldDTO;
import com.example.TTN_E_Commerce.Entity.CategoryMetadataField;
import com.example.TTN_E_Commerce.Exception.CustomBadRequestException;
import com.example.TTN_E_Commerce.Repository.CategoryMetadataFieldRepository;
import com.example.TTN_E_Commerce.Service.services.CategoryMetadataFieldService;
import com.example.TTN_E_Commerce.DTO.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class CategoryMetadataFieldServiceImpl implements CategoryMetadataFieldService{

    private final CategoryMetadataFieldRepository fieldRepository;

    @Override
    public ResponseEntity<?> addField(MetadataFieldDTO dto) {

        if (fieldRepository.findByNameIgnoreCase(dto.getName().trim()).isPresent()) {
            throw new CustomBadRequestException("Metadata field with this name already exists");
        }

        CategoryMetadataField field = new CategoryMetadataField();
        field.setName(dto.getName().trim());
        fieldRepository.save(field);

        Map<String, Object> fieldData = new HashMap<>();
        fieldData.put("id", field.getId());
        fieldData.put("name", field.getName());

        return ResponseEntity.ok(new ApiResponse("Metadata field created successfully", fieldData));
    }

    @Override
    public ResponseEntity<?> getAllFields(int max, int offset, String sortBy, String order, String query) {

        if (!sortBy.equals("id") && !sortBy.equals("name")) {
            throw new CustomBadRequestException("Invalid sort field. Allowed: id, name");
        }

        Sort sort = order.equalsIgnoreCase("desc")
                ? Sort.by(sortBy).descending()
                : Sort.by(sortBy).ascending();

        PageRequest pageable = PageRequest.of(offset, max, sort);
        Page<CategoryMetadataField> page = fieldRepository.findAllWithFilter(
                (query == null || query.isBlank()) ? null : query, pageable);

        Map<String, Object> pageData = new HashMap<>();
        pageData.put("totalElements", page.getTotalElements());
        pageData.put("totalPages", page.getTotalPages());
        pageData.put("currentPage", offset);
        pageData.put("fields", page.getContent().stream().map(f -> {
            Map<String, Object> m = new HashMap<>();
            m.put("id", f.getId());
            m.put("name", f.getName());
            return m;
        }).toList());

        return ResponseEntity.ok(new ApiResponse("Success", pageData));
    }
}