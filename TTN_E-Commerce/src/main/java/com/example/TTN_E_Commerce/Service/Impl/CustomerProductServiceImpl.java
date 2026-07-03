package com.example.TTN_E_Commerce.Service.Impl;

import com.example.TTN_E_Commerce.DTO.ApiResponse;
import com.example.TTN_E_Commerce.Entity.Category;
import com.example.TTN_E_Commerce.Entity.Product;
import com.example.TTN_E_Commerce.Entity.ProductVariation;
import com.example.TTN_E_Commerce.Exception.CustomBadRequestException;
import com.example.TTN_E_Commerce.Exception.NotFoundException;
import com.example.TTN_E_Commerce.Repository.CategoryRepository;
import com.example.TTN_E_Commerce.Repository.ProductRepository;
import com.example.TTN_E_Commerce.Repository.ProductVariationRepository;
import com.example.TTN_E_Commerce.Service.services.CustomerProductService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.*;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class CustomerProductServiceImpl implements CustomerProductService
{
    private final ProductRepository productRepository;
    private final ProductVariationRepository productVariationRepository;
    private final ObjectMapper objectMapper;
    private final CategoryRepository categoryRepository;

    public CustomerProductServiceImpl(ProductRepository productRepository,
                                      ProductVariationRepository productVariationRepository,
                                      ObjectMapper objectMapper,
                                      CategoryRepository categoryRepository) {
        this.productRepository = productRepository;
        this.productVariationRepository = productVariationRepository;
        this.objectMapper = objectMapper;
        this.categoryRepository = categoryRepository;
    }

    private static final List<String> ALLOWED_SORT_FIELDS = List.of("id", "name", "brand");

    @Value("${file.upload-dir:uploads}")
    private String uploadBaseDir;

    @Override
    public ResponseEntity<?> viewProduct(UUID productId) {
        Product product = productRepository.findActiveProductById(productId)
                .orElseThrow(() -> new NotFoundException(
                        "Product not found with id: " + productId));

        if (!productVariationRepository.hasActiveVariations(productId)) {
            throw new NotFoundException(
                    "Product has no active variations");
        }
        List<ProductVariation> activeVariations =
                productVariationRepository.findActiveVariationsByProductId(productId);

        return ResponseEntity.ok(new ApiResponse("Success",
                buildDetailedProductResponse(product, activeVariations)));
    }

    @Override
    public ResponseEntity<?> viewAllProducts(UUID categoryId, String query, int max, int offset,
                                             String sortBy, String order) {

        if (!ALLOWED_SORT_FIELDS.contains(sortBy)) {
            throw new CustomBadRequestException(
                    "Invalid sort field. Allowed: " + ALLOWED_SORT_FIELDS);
        }

        Sort sort = order.equalsIgnoreCase("desc") ? Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();

        Page<Product> page;
        Category category = null;
        boolean isLeaf = true;
        String queryParam = (query == null || query.isBlank()) ? null : query.toLowerCase();

        if (categoryId != null) {
            category = categoryRepository.findById(categoryId)
                    .orElseThrow(() -> new NotFoundException(
                            "Category not found with id: " + categoryId));

            List<UUID> categoryIds = new ArrayList<>();
            categoryIds.add(categoryId);

            isLeaf = categoryRepository.findByParentCategoryId(categoryId).isEmpty();
            if (!isLeaf) {
                collectAllDescendantIds(categoryId, categoryIds);
            }

            if (queryParam != null) {
                page = productRepository.findActiveProductsByCategoryIdsAndQuery(
                        categoryIds, queryParam, PageRequest.of(offset, max, sort));
            } else {
                page = productRepository.findActiveProductsByCategoryIds(
                        categoryIds, PageRequest.of(offset, max, sort));
            }
        } else {
            page = productRepository.findAllActiveProductsWithFilterAndQuery(
                    null, null, queryParam, PageRequest.of(offset, max, sort));
        }

        List<Map<String, Object>> result = page.getContent().stream()
                .filter(p -> productVariationRepository.hasActiveVariations(p.getId()))
                .map(this::buildSummaryProductResponse)
                .toList();

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("categoryId",    categoryId);
        data.put("categoryName",  category != null ? category.getName() : "All Products");
        data.put("isLeaf",        isLeaf);
        data.put("totalElements", page.getTotalElements());
        data.put("totalPages",    page.getTotalPages());
        data.put("currentPage",   offset);
        data.put("pageSize",      max);
        data.put("products",      result);

        return ResponseEntity.ok(new ApiResponse("Success", data));
    }

    @Override
    public ResponseEntity<?> viewSimilarProducts(UUID productId, int max, int offset,
                                                 String sortBy, String order) {

        if (!ALLOWED_SORT_FIELDS.contains(sortBy)) {
            throw new CustomBadRequestException(
                    "Invalid sort field. Allowed: " + ALLOWED_SORT_FIELDS);
        }

        Product product = productRepository.findActiveProductById(productId)
                .orElseThrow(() -> new NotFoundException(
                        "Product not found with id: " + productId));

        Sort sort = order.equalsIgnoreCase("desc")
                ? Sort.by(sortBy).descending()
                : Sort.by(sortBy).ascending();

        Page<Product> page = productRepository.findSimilarProducts(
                product.getCategory().getId(),
                productId,
                PageRequest.of(offset, max, sort));

        List<Map<String, Object>> result = page.getContent().stream()
                .filter(p -> productVariationRepository.hasActiveVariations(p.getId()))
                .map(this::buildSummaryProductResponse)
                .toList();

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("basedOnProductId",   productId);
        data.put("similarityBasis",    "same category");
        data.put("categoryId",         product.getCategory().getId());
        data.put("categoryName",       product.getCategory().getName());
        data.put("totalElements",      page.getTotalElements());
        data.put("totalPages",         page.getTotalPages());
        data.put("currentPage",        offset);
        data.put("pageSize",           max);
        data.put("similarProducts",    result);

        return ResponseEntity.ok(new ApiResponse("Success", data));
    }


    private Map<String, Object> buildDetailedProductResponse(Product product,
                                                             List<ProductVariation> variations) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("id",  product.getId());
        map.put("name",  product.getName());
        map.put("brand", product.getBrand());
        map.put("description", product.getDescription());
        map.put("isActive", product.getIsActive());
        map.put("isCancellable",product.getIsCancellable());
        map.put("isReturnable", product.getIsReturnable());

        map.put("category", buildCategoryChain(product.getCategory()));

        map.put("variations", variations.stream()
                .map(v -> buildDetailedVariationResponse(v, product.getId()))
                .toList());
        return map;
    }
    private Map<String, Object> buildCategoryChain(Category category) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("id",   category.getId());
        map.put("name", category.getName());
        map.put("parent", category.getParentCategory() != null
                ? buildCategoryChain(category.getParentCategory())
                : null);
        return map;
    }
    private Map<String, Object> buildDetailedVariationResponse(ProductVariation v,
                                                               UUID productId) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("id",v.getId());
        map.put("price",  v.getPrice());
        map.put("quantityAvailable", v.getQuantityAvailable());
        map.put("isActive", v.getIsActive());

        if (v.getMetadata() != null) {
            try {
                map.put("metadata", objectMapper.readValue(v.getMetadata(), Map.class));
            } catch (JsonProcessingException e) {
                map.put("metadata", v.getMetadata());
            }
        }
        map.put("primaryImage", v.getPrimaryImageName() != null ? buildImagePath(productId, v.getPrimaryImageName()) : null);

        map.put("secondaryImages", getSecondaryImagePaths(productId, v));

        return map;
    }
    private String buildImagePath(UUID productId, String fileName) {
        if (fileName == null) return null;
        if (fileName.startsWith("http://") || fileName.startsWith("https://") ||
            fileName.startsWith("http:/") || fileName.startsWith("https:/")) {
            return fileName;
        }
        String baseUrl;
        try {
            baseUrl = org.springframework.web.servlet.support.ServletUriComponentsBuilder.fromCurrentContextPath().build().toUriString();
        } catch (Exception e) {
            baseUrl = "http://localhost:8080";
        }
        return baseUrl + "/uploads/products/" + productId + "/variations/" + fileName;
    }

    private List<String> getSecondaryImagePaths(UUID productId, ProductVariation v) {
        List<String> dbImages = v.getSecondaryImages();
        if (dbImages != null && !dbImages.isEmpty()) {
            return dbImages.stream()
                    .map(img -> buildImagePath(productId, img))
                    .collect(Collectors.toList());
        }
        Path dir = Paths.get(uploadBaseDir, "products",
                productId.toString(), "variations");
        if (!Files.exists(dir)) return Collections.emptyList();
        try {
            return Files.list(dir)
                    .filter(p -> p.getFileName().toString()
                            .matches(v.getId() + "_\\d+\\.(jpg|jpeg|png|bmp)"))
                    .map(p -> buildImagePath(productId, p.getFileName().toString()))
                    .sorted()
                    .collect(Collectors.toList());
        } catch (IOException e) {
            return Collections.emptyList();
        }
    }

    private void collectAllDescendantIds(UUID categoryId, List<UUID> ids) {
        for (Category child : categoryRepository.findByParentCategoryId(categoryId)) {
            ids.add(child.getId());
            collectAllDescendantIds(child.getId(), ids);
        }
    }
    private Map<String, Object> buildSummaryProductResponse(Product product) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("id",            product.getId());
        map.put("name",          product.getName());
        map.put("brand",         product.getBrand());
        map.put("description",   product.getDescription());
        map.put("isActive",      product.getIsActive());
        map.put("isCancellable", product.getIsCancellable());
        map.put("isReturnable",  product.getIsReturnable());

        map.put("category", buildCategoryChain(product.getCategory()));

        List<ProductVariation> activeVariations =
                productVariationRepository.findActiveVariationsByProductId(product.getId());

        List<Map<String, Object>> variationImages = activeVariations.stream()
                .map(v -> {
                    Map<String, Object> vMap = new LinkedHashMap<>();
                    vMap.put("variationId", v.getId());
                    vMap.put("price",       v.getPrice());
                    // Primary image: filename from DB → full path computed here
                    vMap.put("primaryImage", v.getPrimaryImageName() != null
                            ? buildImagePath(product.getId(), v.getPrimaryImageName())
                            : null);
                    return vMap;
                })
                .toList();

        map.put("variations", variationImages);

        return map;
    }

}
