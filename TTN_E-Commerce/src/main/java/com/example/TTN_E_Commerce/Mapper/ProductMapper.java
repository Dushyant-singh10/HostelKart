package com.example.TTN_E_Commerce.Mapper;

import com.example.TTN_E_Commerce.DTO.AddProductDTO;
import com.example.TTN_E_Commerce.DTO.ProductResponseDTO;
import com.example.TTN_E_Commerce.Entity.Category;
import com.example.TTN_E_Commerce.Entity.Product;
import com.example.TTN_E_Commerce.Entity.Seller;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface ProductMapper {

    // AddProductDTO → Product entity
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "isActive", constant = "false")
    @Mapping(target = "isDeleted", constant = "false")
    @Mapping(target = "seller", source = "seller")
    @Mapping(target = "category", source = "category")
    @Mapping(target = "name", source = "dto.name")
    @Mapping(target = "brand", source = "dto.brand")
    @Mapping(target = "description", source = "dto.description")
    @Mapping(target = "isCancellable", source = "dto.isCancellable")
    @Mapping(target = "isReturnable", source = "dto.isReturnable")
    Product toEntity(AddProductDTO dto, Seller seller, Category category);

    // Product entity → ProductResponseDTO
    @Mapping(target = "category.id", source = "category.id")
    @Mapping(target = "category.name", source = "category.name")
    ProductResponseDTO toResponseDTO(Product product);
}