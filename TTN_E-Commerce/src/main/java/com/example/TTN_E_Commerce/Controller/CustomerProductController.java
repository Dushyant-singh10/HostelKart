package com.example.TTN_E_Commerce.Controller;

import com.example.TTN_E_Commerce.Service.services.CustomerProductService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/customer/products")
public class CustomerProductController {

    private final CustomerProductService customerProductService;

    public CustomerProductController(CustomerProductService customerProductService) {
        this.customerProductService = customerProductService;
    }

    @GetMapping("/{productId}")
    public ResponseEntity<?> viewProduct(@PathVariable UUID productId) {
        return customerProductService.viewProduct(productId);
    }

    @GetMapping
    public ResponseEntity<?> viewAllProducts(
            @RequestParam(required = false)      UUID categoryId,
            @RequestParam(required = false)      String query,
            @RequestParam(defaultValue = "10")   int max,
            @RequestParam(defaultValue = "0")    int offset,
            @RequestParam(defaultValue = "name") String sortBy,
            @RequestParam(defaultValue = "asc")  String order) {
        return customerProductService.viewAllProducts(categoryId, query, max, offset, sortBy, order);
    }

    @GetMapping("/{productId}/similar")
    public ResponseEntity<?> viewSimilarProducts(
            @PathVariable                        UUID productId,
            @RequestParam(defaultValue = "10")   int max,
            @RequestParam(defaultValue = "0")    int offset,
            @RequestParam(defaultValue = "name") String sortBy,
            @RequestParam(defaultValue = "asc")  String order) {
        return customerProductService.viewSimilarProducts(productId, max, offset, sortBy, order);
    }
}