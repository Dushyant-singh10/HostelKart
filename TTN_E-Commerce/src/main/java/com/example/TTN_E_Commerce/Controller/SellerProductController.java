package com.example.TTN_E_Commerce.Controller;

import com.example.TTN_E_Commerce.DTO.AddProductDTO;
import com.example.TTN_E_Commerce.DTO.AddProductVariationDTO;
import com.example.TTN_E_Commerce.DTO.UpdateProductDTO;
import com.example.TTN_E_Commerce.DTO.UpdateProductVariationDTO;
import com.example.TTN_E_Commerce.Exception.CustomBadRequestException;
import com.example.TTN_E_Commerce.Service.services.SellerProductService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/seller/products")
@RequiredArgsConstructor
public class SellerProductController {

    private final SellerProductService sellerProductService;

    @PostMapping
    public ResponseEntity<?> addProduct(@Valid @RequestBody AddProductDTO dto) {
        return sellerProductService.addProduct(dto);
    }

    @PostMapping(value = "/variations", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> addProductVariation(
            @RequestParam("data")                                    String dataJson,
            @RequestPart(value = "primaryImage",  required = true)  MultipartFile primaryImage,
            @RequestPart(value = "secondaryImages",required = false) MultipartFile[] secondaryImages) {

        AddProductVariationDTO dto = parseJson(dataJson, AddProductVariationDTO.class);

        Map<String, String> errors = new LinkedHashMap<>();
        if (dto.getProductId()         == null) errors.put("productId",         "Product ID is required");
        if (dto.getQuantityAvailable() == null) errors.put("quantityAvailable", "Quantity is required");
        else if (dto.getQuantityAvailable() < 0) errors.put("quantityAvailable","Quantity should be 0 or more");
        if (dto.getPrice()             == null) errors.put("price",             "Price is required");
        else if (dto.getPrice()         < 0)   errors.put("price",             "Price should be 0 or more");
        if (dto.getMetadata()          == null || dto.getMetadata().isEmpty())
            errors.put("metadata",           "Metadata is required");

        if (secondaryImages != null) {
            for (int i = 0; i < secondaryImages.length; i++) {
                MultipartFile sec = secondaryImages[i];
                if (sec != null && !sec.isEmpty()) {
                    String ct = sec.getContentType();
                    if (ct == null || ct.startsWith("text/")) {
                        errors.put("secondaryImages[" + i + "]",
                                "Must be a file, not text. Change type to 'File' in Postman");
                    }
                }
            }
        }
        if (!errors.isEmpty()) return ResponseEntity.badRequest().body(errors);

        return sellerProductService.addProductVariation(dto, primaryImage, secondaryImages);
    }
    @GetMapping
    public ResponseEntity<?> getAllProducts(
            @RequestParam(defaultValue = "10")   int max,
            @RequestParam(defaultValue = "0")    int offset,
            @RequestParam(defaultValue = "name") String sortBy,
            @RequestParam(defaultValue = "asc")  String order,
            @RequestParam(required = false)      String query,
            @RequestParam(required = false)      UUID productId) {
        return sellerProductService.getAllProducts(max, offset, sortBy, order, query, productId);
    }

    @GetMapping("/variations")
    public ResponseEntity<?> getAllProductVariations(
            @RequestParam                        UUID productId,
            @RequestParam(required = false)      UUID variationId,
            @RequestParam(defaultValue = "10")   int max,
            @RequestParam(defaultValue = "0")    int offset,
            @RequestParam(defaultValue = "id")   String sortBy,
            @RequestParam(defaultValue = "asc")  String order,
            @RequestParam(required = false)      String query) {
        return sellerProductService.getAllProductVariations(
                productId, variationId, max, offset, sortBy, order, query);
    }

    @DeleteMapping("/{productId}")
    public ResponseEntity<?> deleteProduct(@PathVariable UUID productId) {
        return sellerProductService.deleteProduct(productId);
    }

    @PutMapping("/{productId}")
    public ResponseEntity<?> updateProduct(
            @PathVariable UUID productId,
            @Valid @RequestBody UpdateProductDTO dto) {
        return sellerProductService.updateProduct(productId, dto);
    }

    @PutMapping(value = "/variations/{variationId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> updateProductVariation(@PathVariable  UUID variationId, @RequestParam("data") String dataJson,
            @RequestPart(value = "primaryImage",  required = false) MultipartFile primaryImage,
            @RequestPart(value = "secondaryImages",required = false) MultipartFile[] secondaryImages) {

        UpdateProductVariationDTO dto = parseJson(dataJson, UpdateProductVariationDTO.class);

        Map<String, String> errors = new LinkedHashMap<>();
        if (dto.getPrice()             != null && dto.getPrice()             < 0)
            errors.put("price",             "Price should be 0 or more");
        if (dto.getQuantityAvailable() != null && dto.getQuantityAvailable() < 0)
            errors.put("quantityAvailable", "Quantity should be 0 or more");


        if (secondaryImages != null) {
            for (int i = 0; i < secondaryImages.length; i++) {
                MultipartFile sec = secondaryImages[i];
                if (sec != null && !sec.isEmpty()) {
                    String ct = sec.getContentType();
                    if (ct == null || ct.startsWith("text/")) {
                        errors.put("secondaryImages[" + i + "]",
                                "Must be a file, not text. Change type to 'File' in Postman");
                    }
                }
            }
        }
        if (!errors.isEmpty()) return ResponseEntity.badRequest().body(errors);
        return sellerProductService.updateProductVariation(variationId, dto, primaryImage, secondaryImages);
    }

    private <T> T parseJson(String json, Class<T> clazz) {
        if (json == null || json.isBlank()) {
            throw new CustomBadRequestException("'data' field cannot be empty");
        }
        try {
            ObjectMapper mapper = new ObjectMapper();
            return mapper.readValue(json, clazz);
        } catch (JsonProcessingException e) {
            throw new CustomBadRequestException(
                    "Invalid JSON format in 'data' field. " + "Please send a valid JSON object. Example: " + "{\"productId\":\"uuid\",\"price\":100,\"quantityAvailable\":5,\"metadata\":{\"color\":\"Red\"}}");
        }
    }
}