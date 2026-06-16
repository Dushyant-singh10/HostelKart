package com.example.TTN_E_Commerce.DTO;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class AddProductDTO {

    @NotBlank(message = "Product name is required")
    private String name;

    @NotBlank(message = "Brand is required")
    private String brand;

    @NotNull(message = "Category ID is required")
    private UUID categoryId;

    private String description;
    private Boolean isCancellable = false;
    private Boolean isReturnable = false;
}