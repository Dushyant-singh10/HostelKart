package com.example.TTN_E_Commerce.Service.Impl;

import com.example.TTN_E_Commerce.DTO.*;
import com.example.TTN_E_Commerce.Entity.*;
import com.example.TTN_E_Commerce.Exception.CustomBadRequestException;
import com.example.TTN_E_Commerce.Exception.NotFoundException;
import com.example.TTN_E_Commerce.Repository.*;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class CartService {

    @Autowired
    private CartRepository cartRepository;

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private ProductVariationRepository productVariationRepository;

    @Autowired
    private ObjectMapper objectMapper;

    private Customer getLoggedInCustomer() {
        String email = (String) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return customerRepository.findByUserEmail(email)
                .orElseThrow(() -> new NotFoundException("Customer not found"));
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

    private CartItemResponseDTO mapToResponseDTO(Cart cart) {
        CartItemResponseDTO dto = new CartItemResponseDTO();
        dto.setId(cart.getId());
        dto.setQuantity(cart.getQuantity());
        dto.setIsWishlistItem(cart.getIsWishlistItem());

        ProductVariation variation = cart.getProductVariation();
        dto.setProductVariationId(variation.getId());
        dto.setPrice(variation.getPrice());

        Product product = variation.getProduct();
        dto.setPrimaryImage(buildImagePath(product.getId(), variation.getPrimaryImageName()));
        dto.setProductId(product.getId());
        dto.setProductName(product.getName());
        dto.setBrand(product.getBrand());

        // Parse metadata JSON
        if (variation.getMetadata() != null) {
            try {
                dto.setMetadata(objectMapper.readValue(variation.getMetadata(), Map.class));
            } catch (JsonProcessingException e) {
                dto.setMetadata(Collections.emptyMap());
            }
        } else {
            dto.setMetadata(Collections.emptyMap());
        }

        return dto;
    }

    public ResponseEntity<?> getCart() {
        Customer customer = getLoggedInCustomer();
        List<Cart> cartItems = cartRepository.findByCustomer(customer);
        List<CartItemResponseDTO> dtoList = cartItems.stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(dtoList);
    }

    @Transactional
    public ResponseEntity<?> addToCart(CartItemRequestDTO dto) {
        Customer customer = getLoggedInCustomer();
        ProductVariation variation = productVariationRepository.findById(dto.getProductVariationId())
                .orElseThrow(() -> new NotFoundException("Product variation not found"));

        if (!variation.getIsActive() || variation.getProduct().getIsDeleted() || !variation.getProduct().getIsActive()) {
            throw new CustomBadRequestException("This product variation is not active or available for sale");
        }

        // Check stock availability
        if (dto.getQuantity() > variation.getQuantityAvailable()) {
            throw new CustomBadRequestException("Requested quantity exceeds available stock (" + variation.getQuantityAvailable() + ")");
        }

        // Check if item already exists in cart
        Optional<Cart> existingCartItem = cartRepository.findByCustomerAndProductVariation(customer, variation);
        Cart cartItem;
        if (existingCartItem.isPresent()) {
            cartItem = existingCartItem.get();
            int newQty = cartItem.getQuantity() + dto.getQuantity();
            if (newQty > variation.getQuantityAvailable()) {
                throw new CustomBadRequestException("Adding " + dto.getQuantity() + " more items exceeds available stock (" + variation.getQuantityAvailable() + ")");
            }
            cartItem.setQuantity(newQty);
        } else {
            cartItem = new Cart();
            cartItem.setCustomer(customer);
            cartItem.setProductVariation(variation);
            cartItem.setQuantity(dto.getQuantity());
            cartItem.setIsWishlistItem(Boolean.TRUE.equals(dto.getIsWishlistItem()));
        }

        cartRepository.save(cartItem);
        return ResponseEntity.ok(new ApiResponse("Item added to cart successfully", mapToResponseDTO(cartItem)));
    }

    @Transactional
    public ResponseEntity<?> updateCartItem(UUID cartItemId, Integer quantity, Boolean isWishlistItem) {
        Customer customer = getLoggedInCustomer();
        Cart cartItem = cartRepository.findById(cartItemId)
                .orElseThrow(() -> new NotFoundException("Cart item not found"));

        // Validate ownership
        if (!cartItem.getCustomer().getUserId().equals(customer.getUserId())) {
            throw new CustomBadRequestException("You are not authorized to update this cart item");
        }

        if (quantity != null) {
            if (quantity < 1) {
                throw new CustomBadRequestException("Quantity must be at least 1");
            }
            ProductVariation variation = cartItem.getProductVariation();
            if (quantity > variation.getQuantityAvailable()) {
                throw new CustomBadRequestException("Requested quantity exceeds available stock (" + variation.getQuantityAvailable() + ")");
            }
            cartItem.setQuantity(quantity);
        }

        if (isWishlistItem != null) {
            cartItem.setIsWishlistItem(isWishlistItem);
        }

        cartRepository.save(cartItem);
        return ResponseEntity.ok(new ApiResponse("Cart item updated successfully", mapToResponseDTO(cartItem)));
    }

    @Transactional
    public ResponseEntity<?> removeFromCart(UUID cartItemId) {
        Customer customer = getLoggedInCustomer();
        Cart cartItem = cartRepository.findById(cartItemId)
                .orElseThrow(() -> new NotFoundException("Cart item not found"));

        // Validate ownership
        if (!cartItem.getCustomer().getUserId().equals(customer.getUserId())) {
            throw new CustomBadRequestException("You are not authorized to delete this cart item");
        }

        cartRepository.delete(cartItem);
        return ResponseEntity.ok(new ApiResponse("Item removed from cart successfully"));
    }

    @Transactional
    public ResponseEntity<?> clearCart() {
        Customer customer = getLoggedInCustomer();
        cartRepository.deleteByCustomer(customer);
        return ResponseEntity.ok(new ApiResponse("Cart cleared successfully"));
    }
}
