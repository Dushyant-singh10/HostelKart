package com.example.TTN_E_Commerce.Service.services;

import com.example.TTN_E_Commerce.DTO.AddProductDTO;
import com.example.TTN_E_Commerce.DTO.AddProductVariationDTO;
import com.example.TTN_E_Commerce.DTO.UpdateProductDTO;
import com.example.TTN_E_Commerce.DTO.UpdateProductVariationDTO;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

public interface SellerProductService {

    ResponseEntity<?> addProduct(AddProductDTO dto);

    ResponseEntity<?> addProductVariation(AddProductVariationDTO dto,
                                          MultipartFile primaryImage,
                                          MultipartFile[] secondaryImages);

    ResponseEntity<?> getAllProducts(int max, int offset, String sortBy, String order,
                                     String query, UUID productId);
    ResponseEntity<?> getAllProductVariations(UUID productId, UUID variationId,
                                              int max, int offset, String sortBy,
                                              String order, String query);

    ResponseEntity<?> deleteProduct(UUID productId);
    ResponseEntity<?> updateProduct(UUID productId, UpdateProductDTO dto);

    ResponseEntity<?> updateProductVariation(UUID variationId,
                                             UpdateProductVariationDTO dto,
                                             MultipartFile primaryImage,
                                             MultipartFile[] secondaryImages);
}