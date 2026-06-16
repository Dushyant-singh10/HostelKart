package com.example.TTN_E_Commerce.DTO;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
public class CategoryResponseDTO {

    private UUID id;
    private String name;
    private List<ParentDTO> parentHierarchy;
    private List<ChildDTO> children;
    private List<FieldValueDTO> metadataFields;

    @Getter
    @AllArgsConstructor
    public static class ParentDTO {
        private UUID id;
        private String name;
    }

    @Getter
    @AllArgsConstructor
    public static class ChildDTO {
        private UUID id;
        private String name;
    }

    @Getter
    @AllArgsConstructor
    public static class FieldValueDTO {
        private UUID fieldId;
        private String fieldName;
        private String possibleValues;
    }
}