package com.example.TTN_E_Commerce.Controller;

import com.example.TTN_E_Commerce.Service.services.SellerCategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/seller/categories")
@RequiredArgsConstructor
public class SellerCategoryController {

    private final SellerCategoryService sellerCategoryService;

    @GetMapping
    public ResponseEntity<?> listLeafCategories() {
        return sellerCategoryService.listLeafCategories();
    }
}