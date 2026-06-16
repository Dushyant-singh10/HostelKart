package com.example.TTN_E_Commerce.DTO;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;
import java.util.UUID;

@Getter
@AllArgsConstructor
public class CategoryFilterDetailsDTO {

    private UUID categoryId;
    private String categoryName;
    private List<MetadataFieldDTO> metadataFields;
    private List<String> brands;
    private Double minPrice;
    private Double maxPrice;

    @Getter
    @AllArgsConstructor
    public static class MetadataFieldDTO {
        private UUID fieldId;
        private String fieldName;
        private String possibleValues;
    }
}