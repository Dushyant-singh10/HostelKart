package com.example.TTN_E_Commerce.DTO;

import jakarta.validation.constraints.Min;
import lombok.Getter;
import lombok.Setter;

import java.util.Map;

@Getter
@Setter
public class UpdateProductVariationDTO {


    @Min(value = 0, message = "Quantity should be 0 or more")
    private Integer quantityAvailable;

    @Min(value = 0, message = "Price should be 0 or more")
    private Double price;

    private Map<String, String> metadata;

    private Boolean isActive;
}