package com.example.TTN_E_Commerce.Service.services;

import org.springframework.http.ResponseEntity;

public interface SellerCategoryService {
    ResponseEntity<?> listLeafCategories();
}
