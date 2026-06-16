package com.example.TTN_E_Commerce.Service.Impl;

import com.example.TTN_E_Commerce.DTO.ApiResponse;
import com.example.TTN_E_Commerce.Entity.Category;
import com.example.TTN_E_Commerce.Entity.Product;
import com.example.TTN_E_Commerce.Entity.ProductVariation;
import com.example.TTN_E_Commerce.Entity.User;
import com.example.TTN_E_Commerce.Exception.CustomBadRequestException;
import com.example.TTN_E_Commerce.Exception.NotFoundException;
import com.example.TTN_E_Commerce.Repository.CategoryRepository;
import com.example.TTN_E_Commerce.Repository.ProductRepository;
import com.example.TTN_E_Commerce.Repository.ProductVariationRepository;
import com.example.TTN_E_Commerce.Repository.SellerRepository;
import com.example.TTN_E_Commerce.Service.services.AdminProductService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdminProductServiceImpl implements AdminProductService {

    private final ProductRepository          productRepository;
    private final ProductVariationRepository productVariationRepository;
    private final SellerRepository sellerRepository;
    private final CategoryRepository         categoryRepository;
    private final EmailService               emailService;
    private final ObjectMapper               objectMapper;

    private static final List<String> ALLOWED_SORT_FIELDS = List.of("id", "name", "brand");
    @Value("${file.upload-dir:uploads}")
    private String uploadBaseDir;

    @Override
    public ResponseEntity<?> getAllProducts(int max, int offset, String sortBy, String order,
                                            UUID sellerId, UUID categoryId, UUID productId) {

        if (!ALLOWED_SORT_FIELDS.contains(sortBy)) {
            throw new CustomBadRequestException(
                    "Invalid sort field. Allowed: " + ALLOWED_SORT_FIELDS);
        }

        if (productId != null) {
            Page<Product> result = productRepository.findProductByIdWithFilters(
                    productId, sellerId, categoryId, PageRequest.of(0, 1));

            if (result.isEmpty()) {
                throw new NotFoundException(
                        "No product found for given productId: " + productId + (sellerId   != null ? ", sellerId: "   + sellerId   : "") + (categoryId != null ? ", categoryId: " + categoryId : ""));
            }

            return ResponseEntity.ok(new ApiResponse("Success", buildDetailedProductResponse(result.getContent().get(0))));
        }

        if (sellerId != null) {
            sellerRepository.findById(sellerId).orElseThrow(() -> new NotFoundException("Seller not found with id: " + sellerId));
        }
        if (categoryId != null) {
            categoryRepository.findById(categoryId)
                    .orElseThrow(() -> new NotFoundException("Category not found with id: " + categoryId));
        }

        Sort sort = order.equalsIgnoreCase("desc") ? Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();

        Page<Product> page = productRepository.findAllActiveProductsWithFilter(sellerId, categoryId, PageRequest.of(offset, max, sort));

        List<Map<String, Object>> result = page.getContent().stream().map(this::buildSummaryProductResponse).toList();

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("totalElements", page.getTotalElements());
        data.put("totalPages",    page.getTotalPages());
        data.put("currentPage",   offset);
        data.put("pageSize",      max);
        data.put("products",      result);

        return ResponseEntity.ok(new ApiResponse("Success", data));
    }

    private Map<String, Object> buildDetailedProductResponse(Product product)
    {
        Map<String , Object> map= new HashMap<>();
        map.put("id", product.getId());
        map.put("name", product.getName());
        map.put("brand", product.getBrand());
        map.put("description",product.getDescription());
        map.put("isActive", product.getIsActive());
        map.put("isCancellable", product.getIsCancellable());
        map.put("isReturnable", product.getIsReturnable());
        map.put("seller", Map.of(
                "id",          product.getSeller().getUserId(),
                "name",        product.getSeller().getUser().getFirstName()+" " + product.getSeller().getUser().getLastName(),
                "companyName", product.getSeller().getCompanyName(),
                "email",       product.getSeller().getUser().getEmail()));
        map.put("category", buildCategoryChain(product.getCategory()));
        List<ProductVariation> variations= productVariationRepository.findAllByProductId(product.getId());
        map.put("variations", variations.stream()
                .map(v -> buildVariationResponse(v, product.getId()))
                .toList());

        return map;
    }

    private Map<String, Object> buildCategoryChain(Category category) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("id",   category.getId());
        map.put("name", category.getName());
        map.put("parent", category.getParentCategory() != null
                ? buildCategoryChain(category.getParentCategory()) : null);
        return map;
    }

    private Map<String, Object> buildVariationResponse(ProductVariation v, UUID productId) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("id", v.getId());
        map.put("price", v.getPrice());
        map.put("quantityAvailable", v.getQuantityAvailable());
        map.put("isActive", v.getIsActive());

        if (v.getMetadata() != null)
        {
            try{
                map.put("metadata", objectMapper.readValue(v.getMetadata(),Map.class));
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }
        }
        map.put("primaryImage", v.getPrimaryImageName() != null
                ? buildImagePath(productId, v.getPrimaryImageName()) : null);
        map.put("secondaryImages", getSecondaryImagePaths(productId, v.getId()));

        return map;
    }
    private String buildImagePath(UUID productId, String fileName)
    {
        return uploadBaseDir+"/products/"+ productId+ "/variations/"+ fileName;
    }
    private List<String> getSecondaryImagePaths(UUID productId, UUID variationId)
    {
        Path dir = Paths.get(uploadBaseDir,"products",productId.toString(),"variations");
        if(!Files.exists(dir))
        {
            return Collections.emptyList();
        }
        try {
            return Files.list(dir)
                    .filter(p -> p.getFileName().toString()
                            .matches(variationId + "_\\d+\\.(jpg|jpeg|png|bmp)"))
                    .map(p -> p.toString().replace("\\", "/"))
                    .sorted()
                    .collect(Collectors.toList());
        } catch (IOException e) {
            return Collections.emptyList();
        }
    }
    @Override
    public ResponseEntity<?> activateProduct(UUID productId) {

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new NotFoundException(
                        "Product not found with id: " + productId));

        if (product.getIsDeleted())
            throw new NotFoundException("Product not found");

        if (product.getIsActive())
            throw new CustomBadRequestException("Product is already active");

        product.setIsActive(true);
        productRepository.save(product);

        User sellerUser = product.getSeller().getUser();
        emailService.sendHtmlMail(
                sellerUser.getEmail(),
                "Your Product Has Been Activated",
                "<p>Dear <b>" + sellerUser.getFirstName() + "</b>,</p>"
                        + "<p>Your product <b>" + product.getName() + "</b>"
                        + " (Brand: " + product.getBrand() + ")"
                        + " has been <b>activated</b> by the admin.</p>"
                        + "<p>It is now visible to customers.</p>");

        return ResponseEntity.ok(new ApiResponse("Product activated successfully",
                Map.of("id", productId, "name", product.getName(), "isActive", true)));
    }

    @Override
    public ResponseEntity<?> deactivateProduct(UUID productId) {

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new NotFoundException(
                        "Product not found with id: " + productId));

        if (product.getIsDeleted())
            throw new NotFoundException("Product not found");

        if (!product.getIsActive())
            throw new CustomBadRequestException("Product is already inactive");

        product.setIsActive(false);
        productRepository.save(product);

        User sellerUser = product.getSeller().getUser();
        emailService.sendHtmlMail(
                sellerUser.getEmail(),
                "Your Product Has Been Deactivated",
                "<p>Dear <b>" + sellerUser.getFirstName() + "</b>,</p>"
                        + "<p>Your product <b>" + product.getName() + "</b>"
                        + " (Brand: " + product.getBrand()
                        + ", Category: " + product.getCategory().getName() + ")"
                        + " has been <b>deactivated</b> by the admin.</p>"
                        + "<p>Contact support if you believe this is an error.</p>");

        return ResponseEntity.ok(new ApiResponse("Product deactivated successfully",
                Map.of("id", productId, "name", product.getName(), "isActive", false)));
    }

    private Map<String, Object> buildSummaryProductResponse(Product product) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("id",product.getId());
        map.put("name", product.getName());
        map.put("brand",product.getBrand());
        map.put("description", product.getDescription());
        map.put("isActive",product.getIsActive());
        map.put("isCancellable", product.getIsCancellable());
        map.put("isReturnable",  product.getIsReturnable());
        map.put("seller", Map.of(
                "id", product.getSeller().getUserId(),
                "name",product.getSeller().getUser().getFirstName() + " " + product.getSeller().getUser().getLastName(),
                "companyName", product.getSeller().getCompanyName()));
        map.put("category", buildCategoryChain(product.getCategory()));

        List<ProductVariation> variations =
                productVariationRepository.findAllByProductId(product.getId());
        map.put("variations", variations.stream().map(v -> {
            Map<String, Object> vMap = new LinkedHashMap<>();
            vMap.put("variationId",  v.getId());
            vMap.put("price",        v.getPrice());
            vMap.put("isActive",     v.getIsActive());
            vMap.put("primaryImage", v.getPrimaryImageName() != null
                    ? buildImagePath(product.getId(), v.getPrimaryImageName()) : null);
            return vMap;
        }).toList());
        return map;
    }
}
