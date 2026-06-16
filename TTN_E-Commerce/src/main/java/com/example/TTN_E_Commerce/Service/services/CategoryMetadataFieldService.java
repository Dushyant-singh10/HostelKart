package com.example.TTN_E_Commerce.Service.services;

import com.example.TTN_E_Commerce.DTO.MetadataFieldDTO;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

public interface CategoryMetadataFieldService {
    public ResponseEntity<?> addField(MetadataFieldDTO dto);
    public ResponseEntity<?> getAllFields(int max, int offset, String sortBy, String order, String query);
}
