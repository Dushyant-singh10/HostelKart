package com.example.TTN_E_Commerce.Service.services;

import org.springframework.http.ResponseEntity;
import java.util.UUID;

public interface CustomerProductService {

    ResponseEntity<?> viewProduct(UUID productId);

    ResponseEntity<?> viewAllProducts(UUID categoryId, int max, int offset,
                                      String sortBy, String order);

    ResponseEntity<?> viewSimilarProducts(UUID productId, int max, int offset,
                                          String sortBy, String order);
}