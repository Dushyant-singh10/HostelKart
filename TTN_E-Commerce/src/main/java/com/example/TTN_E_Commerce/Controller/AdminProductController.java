package com.example.TTN_E_Commerce.Controller;

import com.example.TTN_E_Commerce.DTO.LoginDTO;
import com.example.TTN_E_Commerce.Enum.RoleType;
import com.example.TTN_E_Commerce.Service.Impl.AdminService;
import com.example.TTN_E_Commerce.Service.Impl.LoginService;
import com.example.TTN_E_Commerce.Service.services.AdminProductService;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/admin")
public class AdminProductController {

    @Autowired private AdminProductService adminProductService;

    @GetMapping("/products")
    public ResponseEntity<?> getAllProducts(
            @RequestParam(defaultValue = "10") int max,
            @RequestParam(defaultValue = "0") int offset,
            @RequestParam(defaultValue = "name") String sortBy,
            @RequestParam(defaultValue = "asc") String order,
            @RequestParam(required = false) UUID sellerId,
            @RequestParam(required = false) UUID categoryId,
            @RequestParam(required = false) UUID productId)
    {
        return adminProductService.getAllProducts(max, offset, sortBy, order, sellerId, categoryId, productId);
    }

    @PutMapping("/products/{productId}/activate")
    public ResponseEntity<?> activateProduct(@PathVariable UUID productId) {
        return adminProductService.activateProduct(productId);
    }

    @PutMapping("/products/{productId}/deactivate")
    public ResponseEntity<?> deactivateProduct(@PathVariable UUID productId) {
        return adminProductService.deactivateProduct(productId);
    }
}