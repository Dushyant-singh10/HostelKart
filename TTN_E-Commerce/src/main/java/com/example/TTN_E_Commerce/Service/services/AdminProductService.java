package com.example.TTN_E_Commerce.Service.services;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.UUID;



public interface AdminProductService {


     ResponseEntity<?> getAllProducts(int max, int offset, String sortBy, String order, UUID sellerId, UUID categoryId, UUID productId);

     ResponseEntity<?> activateProduct(UUID productId);

     ResponseEntity<?> deactivateProduct(UUID productId);
}
