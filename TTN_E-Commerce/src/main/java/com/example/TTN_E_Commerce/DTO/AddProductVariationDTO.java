package com.example.TTN_E_Commerce.DTO;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.util.Map;
import java.util.UUID;

@Getter
@Setter
public class AddProductVariationDTO {

    @NotNull(message = "Product ID is required")
    private UUID productId;

    @NotNull(message = "Quantity is required")
    @Min(value = 0, message = "Quantity should be 0 or more")
    private Integer quantityAvailable;

    @NotNull(message = "Price is required")
    @Min(value = 0, message = "Price should be 0 or more")
    private Double price;

    @NotNull(message = "Metadata is required. At least one metadata field-value must be provided")
    private Map<String, String> metadata;

    private java.util.List<String> secondaryImages;
}