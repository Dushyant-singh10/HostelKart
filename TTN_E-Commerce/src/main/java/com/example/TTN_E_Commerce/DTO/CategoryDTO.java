package com.example.TTN_E_Commerce.DTO;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class CategoryDTO {

    @NotBlank(message = "There should be any valid Category Name")
    private String name;

    private UUID parentCategory;
}
