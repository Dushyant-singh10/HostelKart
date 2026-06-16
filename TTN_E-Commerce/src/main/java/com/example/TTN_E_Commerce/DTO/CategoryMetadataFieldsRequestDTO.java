package com.example.TTN_E_Commerce.DTO;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.UUID;

@Getter
@Setter
public class CategoryMetadataFieldsRequestDTO {

    @NotNull(message = "Category ID is required")
    private UUID categoryId;

    @NotEmpty(message = "At least one field entry is required")
    @Valid
    private List<FieldEntry> fields;

    @Getter
    @Setter
    public static class FieldEntry {

        @NotNull(message = "Metadata field ID is required")
        private UUID fieldId;

        @NotEmpty(message = "At least one value is required for each field")
        private List<String> values;
    }
}