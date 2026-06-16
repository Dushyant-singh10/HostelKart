package com.example.TTN_E_Commerce.DTO;

import lombok.Builder;
import lombok.Getter;

import java.util.UUID;

@Getter
@Builder
public class ProductResponseDTO {

    private UUID id;
    private String name;
    private String brand;
    private String description;
    private Boolean isCancellable;
    private Boolean isReturnable;
    private Boolean isActive;
    private CategoryInfo category;

    @Getter
    @Builder
    public static class CategoryInfo {
        private UUID id;
        private String name;
    }
}