package com.example.TTN_E_Commerce.DTO;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class CategoryUpdateDTO {
    @NotBlank(message = "There should be any valid Category Name")
    private String name;

    @NotNull(message = "Please pass a valid Category Id")
    private UUID id;
}
