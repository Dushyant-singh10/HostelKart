package com.example.TTN_E_Commerce.Controller;

import com.example.TTN_E_Commerce.Service.services.CustomerCategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/customer/categories")
@RequiredArgsConstructor
public class CustomerCategoryController {

    private final CustomerCategoryService customerCategoryService;

    @GetMapping
    public ResponseEntity<?> listCategories(
            @RequestParam(required = false) UUID categoryId) {
        return customerCategoryService.listCategories(categoryId);
    }

    @GetMapping("/filter-details")
    public ResponseEntity<?> getCategoryFilterDetails(
            @RequestParam UUID categoryId) {
        return customerCategoryService.getCategoryFilterDetails(categoryId);
    }
}