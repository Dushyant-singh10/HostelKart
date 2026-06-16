package com.example.TTN_E_Commerce.Service.Impl;

import com.example.TTN_E_Commerce.DTO.*;
import com.example.TTN_E_Commerce.Entity.*;
import com.example.TTN_E_Commerce.Exception.CustomBadRequestException;
import com.example.TTN_E_Commerce.Exception.NotFoundException;
import com.example.TTN_E_Commerce.Repository.*;
import com.example.TTN_E_Commerce.Service.services.SellerProductService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.*;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SellerProductServiceImpl implements SellerProductService {

    private final ProductRepository productRepository;
    private final ProductVariationRepository productVariationRepository;
    private final CategoryRepository categoryRepository;
    private final SellerRepository sellerRepository;
    private final CategoryMetadataFieldValuesRepository fieldValuesRepository;
    private final ObjectMapper objectMapper;
    private final EmailService emailService;

    private static final List<String> ALLOWED_IMAGE_TYPES =
            List.of("image/jpeg", "image/jpg", "image/png", "image/bmp");

    private static final List<String> ALLOWED_SORT_FIELDS =
            List.of("id", "name", "brand", "price");

    private static final List<String> ALLOWED_VARIATION_SORT_FIELDS =
            List.of("id", "price", "quantityAvailable");

    @Value("${app.admin.email}")
    private String adminEmail;

    @Value("${file.upload-dir:uploads}")
    private String uploadBaseDir;

    private Seller getLoggedInSeller() {
        String email = (String) SecurityContextHolder.getContext()
                .getAuthentication().getPrincipal();
        return sellerRepository.findByUserEmail(email)
                .orElseThrow(() -> new NotFoundException("Seller not found"));
    }

    private String savePrimaryImage(MultipartFile file,
                                    UUID productId,
                                    UUID variationId) throws IOException {
        validateImageFile(file);

        String ext= getExtension(file.getOriginalFilename());
        String fileName = variationId + ext;
        Path dir = Paths.get(uploadBaseDir, "products", productId.toString(), "variations");
        Files.createDirectories(dir);
        Files.copy(file.getInputStream(), dir.resolve(fileName), StandardCopyOption.REPLACE_EXISTING);
        return fileName;
    }

    private void saveSecondaryImages(MultipartFile[] files, UUID productId, UUID variationId) throws IOException {
        Path dir = Paths.get(uploadBaseDir, "products", productId.toString(), "variations");
        Files.createDirectories(dir);

        for (int i = 0; i < files.length; i++) {
            MultipartFile file = files[i];
            if (file == null || file.isEmpty()) continue;

            validateImageFile(file);

            String ext = getExtension(file.getOriginalFilename());
            String fileName = variationId + "_" + (i + 1) + ext;
            Files.copy(file.getInputStream(), dir.resolve(fileName), StandardCopyOption.REPLACE_EXISTING);
        }
    }

    private List<String> getSecondaryImagePaths(UUID productId, UUID variationId) {
        Path dir = Paths.get(uploadBaseDir, "products", productId.toString(), "variations");
        if (!Files.exists(dir)) return Collections.emptyList();
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

    private void deleteOldPrimaryImage(UUID productId, String primaryImageName) {
        if (primaryImageName == null) return;
        try {
            Path file = Paths.get(uploadBaseDir, "products",
                    productId.toString(), "variations", primaryImageName);
            Files.deleteIfExists(file);
        } catch (IOException ignored) {}
    }

    private void deleteOldSecondaryImages(UUID productId, UUID variationId) {
        Path dir = Paths.get(uploadBaseDir, "products", productId.toString(), "variations");
        if (!Files.exists(dir)) return;
        try {
            Files.list(dir)
                    .filter(p -> p.getFileName().toString().matches(variationId + "_\\d+\\.(jpg|jpeg|png|bmp)"))
                    .forEach(p -> { try { Files.deleteIfExists(p); }
                    catch (IOException ignored) {} });
        } catch (IOException ignored) {}
    }

    private void validateImageFile(MultipartFile file) {
        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_IMAGE_TYPES.contains(contentType.toLowerCase())) {
            throw new CustomBadRequestException("Invalid image format for '" + file.getOriginalFilename() + "'. Allowed: jpg, jpeg, png, bmp");
        }
        String original = file.getOriginalFilename();
        if (original == null || !original.contains(".")) {
            throw new CustomBadRequestException("Invalid file name: " + original);
        }
    }

    private String getExtension(String filename) {
        return filename.substring(filename.lastIndexOf(".")).toLowerCase();
    }
    private Map<String, List<String>> getAllowedValuesMap(UUID categoryId) {
        List<CategoryMetadataFieldValues> allowedFieldValues =
                fieldValuesRepository.findByCategoryId(categoryId);
        if (allowedFieldValues.isEmpty()) {
            throw new CustomBadRequestException("No metadata fields defined for this product's category");
        }
        Map<String, List<String>> map = new HashMap<>();
        for (CategoryMetadataFieldValues cfv : allowedFieldValues) {
            map.put(cfv.getField().getName().toLowerCase(),
                    Arrays.asList(cfv.getFieldValues().split(",")));
        }
        return map;
    }

    private void validateMetadata(Map<String, String> metadata, Map<String, List<String>> allowedValuesMap) {
        for (Map.Entry<String, String> entry : metadata.entrySet()) {
            String fieldName = entry.getKey().toLowerCase();
            String sentValue = entry.getValue();

            if (sentValue == null || sentValue.isBlank()) {
                throw new CustomBadRequestException(
                        "Value for metadata field '" + entry.getKey() + "' cannot be blank");
            }
            if (!allowedValuesMap.containsKey(fieldName)) {
                throw new CustomBadRequestException(
                        "Metadata field " + entry.getKey() + " is not defined for this category. Allowed fields: " + allowedValuesMap.keySet());
            }
            List<String> allowedValues = allowedValuesMap.get(fieldName);
            boolean valueAllowed = allowedValues.stream()
                    .anyMatch(v -> v.trim().equalsIgnoreCase(sentValue));
            if (!valueAllowed) {
                throw new CustomBadRequestException(
                        "Value '" + sentValue + "' is not allowed for field '" + entry.getKey() + "'. Allowed values: " + allowedValues);
            }
        }
    }

    @Override
    public ResponseEntity<?> addProduct(AddProductDTO dto) {

        Seller seller = getLoggedInSeller();
        Category category = categoryRepository.findById(dto.getCategoryId())
                .orElseThrow(() -> new NotFoundException("Category not found with id: " + dto.getCategoryId()));

        if (!categoryRepository.findByParentCategoryId(category.getId()).isEmpty()) {
            throw new CustomBadRequestException("Category must be a leaf node. Selected category has sub-categories");
        }

        productRepository.findByNameIgnoreCaseAndBrandIgnoreCaseAndSeller_UserId(dto.getName(), dto.getBrand(), seller.getUserId())
                .ifPresent(p -> { throw new CustomBadRequestException("Product with same name under this brand already exists for this seller"); });

        Product product = new Product();
        product.setName(dto.getName());
        product.setBrand(dto.getBrand());
        product.setDescription(dto.getDescription());
        product.setCategory(category);
        product.setSeller(seller);
        product.setIsCancellable(dto.getIsCancellable() != null ? dto.getIsCancellable() : false);
        product.setIsReturnable(dto.getIsReturnable()   != null ? dto.getIsReturnable()  : false);
        product.setIsActive(false);
        product.setIsDeleted(false);

        productRepository.save(product);

        emailService.sendHtmlMail(adminEmail, "New Product Pending Activation",
                "<p>Seller <b>" + seller.getUser().getFirstName() + " " + seller.getUser().getLastName() + "</b> has added product: <b>" + product.getName() + "</b>. Please activate from Admin panel.</p>");

        Map<String, Object> data = new HashMap<>();
        data.put("id",            product.getId());
        data.put("name",          product.getName());
        data.put("brand",         product.getBrand());
        data.put("description",   product.getDescription());
        data.put("category",      product.getCategory().getName());
        data.put("isCancellable", product.getIsCancellable());
        data.put("isReturnable",  product.getIsReturnable());
        data.put("isActive",      product.getIsActive());

        return ResponseEntity.ok(new ApiResponse(
                "Product created successfully. It is inactive by default and will be activated by admin.", data));
    }

    @Override
    public ResponseEntity<?> addProductVariation(AddProductVariationDTO dto, MultipartFile primaryImage, MultipartFile[] secondaryImages) {
        Seller seller = getLoggedInSeller();

        Product product = productRepository.findById(dto.getProductId())
                .orElseThrow(() -> new NotFoundException("Product not found with id: " + dto.getProductId()));

        if (product.getIsDeleted()) throw new CustomBadRequestException("Product is deleted");
        if (!product.getIsActive()) throw new CustomBadRequestException("Product is not active. Cannot add variation to an inactive product");
        if (!product.getSeller().getUserId().equals(seller.getUserId()))
            throw new CustomBadRequestException("You are not authorized to add a variation to this product");
        if (dto.getMetadata() == null || dto.getMetadata().isEmpty())
            throw new CustomBadRequestException("At least one metadata field-value must be provided");

        Map<String, List<String>> allowedValuesMap = getAllowedValuesMap(product.getCategory().getId());
        validateMetadata(dto.getMetadata(), allowedValuesMap);

        List<ProductVariation> existingVariations = productVariationRepository.findAllByProductId(product.getId());
        if (!existingVariations.isEmpty()) {
            try {
                Map<?, ?> existingKeys = objectMapper.readValue(existingVariations.get(0).getMetadata(), Map.class);
                Set<String> existingKeySet = new HashSet<>();
                existingKeys.keySet().forEach(k -> existingKeySet.add(k.toString().toLowerCase()));
                Set<String> newKeySet = new HashSet<>();
                dto.getMetadata().keySet().forEach(k -> newKeySet.add(k.toLowerCase()));
                if (!existingKeySet.equals(newKeySet)) {
                    throw new CustomBadRequestException(
                            "All variations of a product must have the same metadata structure. "
                                    + "Expected keys: " + existingKeys.keySet());
                }
            } catch (JsonProcessingException e) {
                throw new CustomBadRequestException("Invalid existing metadata format");
            }
        }

        String metadataJson;
        try {
            metadataJson = objectMapper.writeValueAsString(dto.getMetadata());
        } catch (JsonProcessingException e) {
            throw new CustomBadRequestException("Invalid metadata format");
        }

        ProductVariation variation = new ProductVariation();
        variation.setProduct(product);
        variation.setQuantityAvailable(dto.getQuantityAvailable());
        variation.setPrice(dto.getPrice());
        variation.setMetadata(metadataJson);
        variation.setIsActive(true);

        productVariationRepository.save(variation);

        if (primaryImage != null && !primaryImage.isEmpty()) {
            try {
                String fileName = savePrimaryImage(primaryImage, product.getId(), variation.getId());
                variation.setPrimaryImageName(fileName);
                productVariationRepository.save(variation);
            } catch (IOException e) {
                throw new CustomBadRequestException("Failed to save primary image: " + e.getMessage());
            }
        }

        if (secondaryImages != null && secondaryImages.length > 0) {
            try {
                saveSecondaryImages(secondaryImages, product.getId(), variation.getId());
            } catch (IOException e) {
                throw new CustomBadRequestException("Failed to save secondary images: " + e.getMessage());
            }
        }

        List<String> secondaryPaths =
                getSecondaryImagePaths(product.getId(), variation.getId());

        Map<String, Object> data = new HashMap<>();
        data.put("id",                variation.getId());
        data.put("productId",         product.getId());
        data.put("productName",       product.getName());
        data.put("price",             variation.getPrice());
        data.put("quantityAvailable", variation.getQuantityAvailable());
        data.put("metadata",          dto.getMetadata());
        data.put("isActive",          variation.getIsActive());
        data.put("primaryImage",      variation.getPrimaryImageName());  // from DB
        data.put("secondaryImages",   secondaryPaths);                   // computed from disk

        return ResponseEntity.ok(new ApiResponse("Product variation added successfully", data));
    }

    @Override
    public ResponseEntity<?> getAllProducts(int max, int offset, String sortBy,
                                            String order, String query, UUID productId) {

        if (!ALLOWED_SORT_FIELDS.contains(sortBy)) {
            throw new CustomBadRequestException(
                    "Invalid sort field. Allowed: " + ALLOWED_SORT_FIELDS);
        }

        Seller seller = getLoggedInSeller();

        if (productId != null) {
            Product product = productRepository.findById(productId)
                    .orElseThrow(() -> new NotFoundException(
                            "Product not found with id: " + productId));
            if (product.getIsDeleted()) throw new NotFoundException("Product not found");
            if (!product.getSeller().getUserId().equals(seller.getUserId()))
                throw new CustomBadRequestException(
                        "You are not authorized to view this product");
            return ResponseEntity.ok(new ApiResponse("Success", buildProductResponse(product)));
        }

        String resolvedSortBy = sortBy.equals("price") ? "name" : sortBy;
        Sort sort = order.equalsIgnoreCase("desc") ? Sort.by(resolvedSortBy).descending() : Sort.by(resolvedSortBy).ascending();

        String queryParam = (query == null || query.isBlank()) ? null : query.toLowerCase();

        Page<Product> page = productRepository.findActiveProductsBySellerWithFilter(
                seller.getUserId(), queryParam, PageRequest.of(offset, max, sort));

        List<Map<String, Object>> result = page.getContent().stream()
                .map(this::buildProductResponse).toList();

        Map<String, Object> data = new HashMap<>();
        data.put("totalElements", page.getTotalElements());
        data.put("totalPages",    page.getTotalPages());
        data.put("currentPage",   offset);
        data.put("pageSize",      max);
        data.put("products",      result);

        return ResponseEntity.ok(new ApiResponse("Success", data));
    }

    @Override
    public ResponseEntity<?> getAllProductVariations(UUID productId, UUID variationId, int max, int offset, String sortBy, String order, String query) {

        if (!ALLOWED_VARIATION_SORT_FIELDS.contains(sortBy)) {
            throw new CustomBadRequestException(
                    "Invalid sort field. Allowed: " + ALLOWED_VARIATION_SORT_FIELDS);
        }

        Seller seller = getLoggedInSeller();

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new NotFoundException(
                        "Product not found with id: " + productId));
        if (product.getIsDeleted()) throw new NotFoundException("Product not found");
        if (!product.getSeller().getUserId().equals(seller.getUserId()))
            throw new CustomBadRequestException(
                    "You are not authorized to view variations of this product");

        if (variationId != null) {
            ProductVariation variation = productVariationRepository.findById(variationId)
                    .orElseThrow(() -> new NotFoundException(
                            "Variation not found with id: " + variationId));
            if (!variation.getProduct().getId().equals(productId))
                throw new CustomBadRequestException(
                        "Variation does not belong to product id: " + productId);
            return ResponseEntity.ok(new ApiResponse("Success", buildVariationResponse(variation)));
        }

        Sort sort = order.equalsIgnoreCase("desc") ? Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        String queryParam = (query == null || query.isBlank()) ? null : query.toLowerCase();

        Page<ProductVariation> page = productVariationRepository.findByProductIdWithFilter(productId, queryParam, PageRequest.of(offset, max, sort));

        List<Map<String, Object>> result = page.getContent().stream()
                .map(this::buildVariationResponse).toList();

        Map<String, Object> data = new HashMap<>();
        data.put("productId",     productId);
        data.put("productName",   product.getName());
        data.put("totalElements", page.getTotalElements());
        data.put("totalPages",    page.getTotalPages());
        data.put("currentPage",   offset);
        data.put("pageSize",      max);
        data.put("variations",    result);

        return ResponseEntity.ok(new ApiResponse("Success", data));
    }

    @Override
    public ResponseEntity<?> deleteProduct(UUID productId) {
        Seller seller = getLoggedInSeller();

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new NotFoundException(
                        "Product not found with id: " + productId));

        if (product.getIsDeleted()) throw new NotFoundException("Product not found");
        if (!product.getSeller().getUserId().equals(seller.getUserId()))
            throw new CustomBadRequestException(
                    "You are not authorized to delete this product");

        product.setIsDeleted(true);
        productRepository.save(product);

        return ResponseEntity.ok(new ApiResponse(
                "Product deleted successfully", Map.of("id", productId)));
    }

    @Override
    public ResponseEntity<?> updateProduct(UUID productId, UpdateProductDTO dto) {

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new NotFoundException("Product not found with this Id"));
        if (product.getIsDeleted()) throw new NotFoundException("Product not found");

        Seller seller = getLoggedInSeller();
        if (!product.getSeller().getUserId().equals(seller.getUserId()))
            throw new CustomBadRequestException(
                    "You are not authorized to update this product");

        if (dto.getName() == null && dto.getDescription() == null
                && dto.getIsCancellable() == null && dto.getIsReturnable() == null) {
            throw new CustomBadRequestException(
                    "At least one field must be provided for update: "
                            + "name, description, isCancellable, isReturnable");
        }

        if (dto.getName() != null && !dto.getName().isBlank()) {
            String newName = dto.getName().trim();
            productRepository.findByNameIgnoreCaseAndBrandIgnoreCaseAndSeller_UserId(
                            newName, product.getBrand(), seller.getUserId())
                    .ifPresent(p -> { throw new CustomBadRequestException(
                            "Product with name '" + newName + "' and brand '"
                                    + product.getBrand() + "' already exists for this seller"); });
            product.setName(newName);
        }

        if (dto.getDescription()   != null) product.setDescription(dto.getDescription());
        if (dto.getIsCancellable() != null) product.setIsCancellable(dto.getIsCancellable());
        if (dto.getIsReturnable()  != null) product.setIsReturnable(dto.getIsReturnable());

        productRepository.save(product);

        Map<String, Object> data = new HashMap<>();
        data.put("id",            product.getId());
        data.put("name",          product.getName());
        data.put("description",   product.getDescription());
        data.put("isCancellable", product.getIsCancellable());
        data.put("isReturnable",  product.getIsReturnable());
        data.put("isActive",      product.getIsActive());

        return ResponseEntity.ok(new ApiResponse("Product updated successfully", data));
    }

    @Override
    public ResponseEntity<?> updateProductVariation(UUID variationId,
                                                    UpdateProductVariationDTO dto,
                                                    MultipartFile primaryImage,
                                                    MultipartFile[] secondaryImages) {

        Seller seller = getLoggedInSeller();

        ProductVariation variation = productVariationRepository.findById(variationId)
                .orElseThrow(() -> new NotFoundException(
                        "Variation not found with id: " + variationId));

        Product product = variation.getProduct();

        if (product.getIsDeleted())
            throw new CustomBadRequestException("Cannot update variation — product is deleted");
        if (!product.getIsActive())
            throw new CustomBadRequestException("Cannot update variation — product is not active");
        if (!product.getSeller().getUserId().equals(seller.getUserId()))
            throw new CustomBadRequestException(
                    "You are not authorized to update this product variation");

        boolean hasData = dto.getQuantityAvailable() != null || dto.getPrice() != null
                || dto.getMetadata() != null || dto.getIsActive() != null;
        boolean hasImages = (primaryImage != null && !primaryImage.isEmpty())
                || (secondaryImages != null && secondaryImages.length > 0);

        if (!hasData && !hasImages) {
            throw new CustomBadRequestException(
                    "At least one field must be provided for update");
        }

        if (dto.getQuantityAvailable() != null)
            variation.setQuantityAvailable(dto.getQuantityAvailable());
        if (dto.getPrice()    != null) variation.setPrice(dto.getPrice());
        if (dto.getIsActive() != null) variation.setIsActive(dto.getIsActive());

        if (dto.getMetadata() != null && !dto.getMetadata().isEmpty()) {
            Map<String, List<String>> allowedValuesMap =
                    getAllowedValuesMap(product.getCategory().getId());
            validateMetadata(dto.getMetadata(), allowedValuesMap);

            List<ProductVariation> otherVariations =
                    productVariationRepository.findAllByProductId(product.getId())
                            .stream().filter(v -> !v.getId().equals(variationId)).toList();

            if (!otherVariations.isEmpty()) {
                try {
                    Map<?, ?> existingKeys = objectMapper.readValue(
                            otherVariations.get(0).getMetadata(), Map.class);
                    Set<String> existingKeySet = new HashSet<>();
                    existingKeys.keySet()
                            .forEach(k -> existingKeySet.add(k.toString().toLowerCase()));
                    Set<String> newKeySet = new HashSet<>();
                    dto.getMetadata().keySet().forEach(k -> newKeySet.add(k.toLowerCase()));
                    if (!existingKeySet.equals(newKeySet)) {
                        throw new CustomBadRequestException(
                                "All variations of a product must have the same metadata structure. "
                                        + "Expected keys: " + existingKeys.keySet());
                    }
                } catch (JsonProcessingException e) {
                    throw new CustomBadRequestException("Invalid existing metadata format");
                }
            }

            try {
                variation.setMetadata(objectMapper.writeValueAsString(dto.getMetadata()));
            } catch (JsonProcessingException e) {
                throw new CustomBadRequestException("Invalid metadata format");
            }
        }

        if (primaryImage != null && !primaryImage.isEmpty()) {
            deleteOldPrimaryImage(product.getId(), variation.getPrimaryImageName());
            try {
                String fileName = savePrimaryImage(primaryImage, product.getId(), variationId);
                variation.setPrimaryImageName(fileName); // update DB
            } catch (IOException e) {
                throw new CustomBadRequestException(
                        "Failed to save primary image: " + e.getMessage());
            }
        }

        if (secondaryImages != null && secondaryImages.length > 0) {
            deleteOldSecondaryImages(product.getId(), variationId);
            try {
                saveSecondaryImages(secondaryImages, product.getId(), variationId);
            } catch (IOException e) {
                throw new CustomBadRequestException(
                        "Failed to save secondary images: " + e.getMessage());
            }
        }

        productVariationRepository.save(variation);

        Map<?, ?> metadataMap = null;
        if (variation.getMetadata() != null) {
            try {
                metadataMap = objectMapper.readValue(variation.getMetadata(), Map.class);
            } catch (JsonProcessingException ignored) {}
        }

        // Compute secondary image paths from disk (no DB needed)
        List<String> secondaryPaths =
                getSecondaryImagePaths(product.getId(), variationId);

        Map<String, Object> data = new HashMap<>();
        data.put("id",                variation.getId());
        data.put("productId",         product.getId());
        data.put("productName",       product.getName());
        data.put("price",             variation.getPrice());
        data.put("quantityAvailable", variation.getQuantityAvailable());
        data.put("isActive",          variation.getIsActive());
        data.put("metadata",          metadataMap);
        data.put("primaryImage",      variation.getPrimaryImageName());  // from DB
        data.put("secondaryImages",   secondaryPaths);                   // computed from disk

        return ResponseEntity.ok(new ApiResponse("Product variation updated successfully", data));
    }

    private Map<String, Object> buildProductResponse(Product product) {
        Map<String, Object> map = new HashMap<>();
        map.put("id",            product.getId());
        map.put("name",          product.getName());
        map.put("brand",         product.getBrand());
        map.put("description",   product.getDescription());
        map.put("isActive",      product.getIsActive());
        map.put("isCancellable", product.getIsCancellable());
        map.put("isReturnable",  product.getIsReturnable());
        map.put("category", Map.of(
                "id",   product.getCategory().getId(),
                "name", product.getCategory().getName()));
        map.put("variationsCount",
                productVariationRepository.findAllByProductId(product.getId()).size());
        return map;
    }

    private Map<String, Object> buildVariationResponse(ProductVariation v) {
        Map<String, Object> map = new HashMap<>();
        map.put("id",                v.getId());
        map.put("productId",         v.getProduct().getId());
        map.put("price",             v.getPrice());
        map.put("quantityAvailable", v.getQuantityAvailable());
        map.put("isActive",          v.getIsActive());
        map.put("primaryImage",      v.getPrimaryImageName());
        map.put("secondaryImages",
                getSecondaryImagePaths(v.getProduct().getId(), v.getId()));

        if (v.getMetadata() != null) {
            try {
                map.put("metadata", objectMapper.readValue(v.getMetadata(), Map.class));
            } catch (JsonProcessingException e) {
                map.put("metadata", v.getMetadata());
            }
        }
        return map;
    }
}