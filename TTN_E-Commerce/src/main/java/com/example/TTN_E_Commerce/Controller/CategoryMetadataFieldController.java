package com.example.TTN_E_Commerce.Controller;

import com.example.TTN_E_Commerce.DTO.MetadataFieldDTO;
import com.example.TTN_E_Commerce.Service.services.CategoryMetadataFieldService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin/metadata-fields")
@RequiredArgsConstructor
public class CategoryMetadataFieldController {

    private final CategoryMetadataFieldService service;

    @PostMapping
    public ResponseEntity<?> addField(@Valid @RequestBody MetadataFieldDTO dto) {
        return service.addField(dto);
    }

    @GetMapping
    public ResponseEntity<?> getAllFields(
            @RequestParam(defaultValue = "10") int max,
            @RequestParam(defaultValue = "0") int offset,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "asc") String order,
            @RequestParam(required = false) String query) {
        return service.getAllFields(max, offset, sortBy, order, query);
    }
}