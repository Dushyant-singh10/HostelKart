package com.example.TTN_E_Commerce.DTO;

import lombok.Getter;
import lombok.Setter;

import java.util.Map;
import java.util.UUID;

@Getter
@Setter
public class CartItemResponseDTO {
    private UUID id;
    private UUID productId;
    private String productName;
    private String brand;
    private UUID productVariationId;
    private Double price;
    private Integer quantity;
    private Map<String, String> metadata;
    private String primaryImage;
    private Boolean isWishlistItem;
}
