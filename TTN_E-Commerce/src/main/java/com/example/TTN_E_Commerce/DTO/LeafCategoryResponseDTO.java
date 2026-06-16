package com.example.TTN_E_Commerce.DTO;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;
import java.util.UUID;

@Getter
@AllArgsConstructor
public class LeafCategoryResponseDTO {

    private UUID id;
    private String name;
    private List<ParentDTO> parentHierarchy;
    private List<MetadataFieldDTO> metadataFields;

    @Getter
    @AllArgsConstructor
    public static class ParentDTO {
        private UUID id;
        private String name;
    }

    @Getter
    @AllArgsConstructor
    public static class MetadataFieldDTO {
        private UUID fieldId;
        private String fieldName;
        private String possibleValues;
    }
}