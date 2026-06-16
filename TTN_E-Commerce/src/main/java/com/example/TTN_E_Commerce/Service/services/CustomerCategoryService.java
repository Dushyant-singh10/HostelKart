package com.example.TTN_E_Commerce.Service.services;

import org.springframework.http.ResponseEntity;

import java.util.UUID;

public interface CustomerCategoryService{
    public ResponseEntity<?> listCategories(UUID categoryId);
    public ResponseEntity<?> getCategoryFilterDetails(UUID categoryId);
}
