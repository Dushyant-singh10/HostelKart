package com.example.TTN_E_Commerce.Controller;

import com.example.TTN_E_Commerce.DTO.CartItemRequestDTO;
import com.example.TTN_E_Commerce.Service.Impl.CartService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/customer/cart")
public class CartController {

    @Autowired
    private CartService cartService;

    @GetMapping
    public ResponseEntity<?> getCart() {
        return cartService.getCart();
    }

    @PostMapping
    public ResponseEntity<?> addToCart(@Valid @RequestBody CartItemRequestDTO dto) {
        return cartService.addToCart(dto);
    }

    @PutMapping("/{cartItemId}")
    public ResponseEntity<?> updateCartItem(
            @PathVariable UUID cartItemId,
            @RequestParam(required = false) Integer quantity,
            @RequestParam(required = false) Boolean isWishlistItem) {
        return cartService.updateCartItem(cartItemId, quantity, isWishlistItem);
    }

    @DeleteMapping("/{cartItemId}")
    public ResponseEntity<?> removeFromCart(@PathVariable UUID cartItemId) {
        return cartService.removeFromCart(cartItemId);
    }

    @DeleteMapping
    public ResponseEntity<?> clearCart() {
        return cartService.clearCart();
    }
}
