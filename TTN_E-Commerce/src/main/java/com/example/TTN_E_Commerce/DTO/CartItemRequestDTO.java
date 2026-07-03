package com.example.TTN_E_Commerce.DTO;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class CartItemRequestDTO {

    @NotNull(message = "Product variation ID is required")
    private UUID productVariationId;

    @NotNull(message = "Quantity is required")
    @Min(value = 1, message = "Quantity must be at least 1")
    private Integer quantity;

    private Boolean isWishlistItem = false;
}
