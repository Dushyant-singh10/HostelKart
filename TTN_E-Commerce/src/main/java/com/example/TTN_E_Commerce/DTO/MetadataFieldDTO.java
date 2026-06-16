package com.example.TTN_E_Commerce.DTO;


import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MetadataFieldDTO {

    @NotBlank(message = "Field name is required")
    private String name;
}
