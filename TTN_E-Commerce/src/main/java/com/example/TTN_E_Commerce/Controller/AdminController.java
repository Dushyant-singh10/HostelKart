package com.example.TTN_E_Commerce.Controller;

import com.example.TTN_E_Commerce.DTO.LoginDTO;
import com.example.TTN_E_Commerce.Enum.RoleType;
import com.example.TTN_E_Commerce.Service.Impl.AdminService;
import com.example.TTN_E_Commerce.Service.Impl.LoginService;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/admin")
public class AdminController {

    @Autowired
    private  AdminService adminService;

    @Autowired
    private LoginService loginService;

    @PostMapping("/login")
    ResponseEntity<?> login(@Valid @RequestBody LoginDTO loginDTO, HttpServletResponse response) {
        return loginService.login(loginDTO, RoleType.ADMIN, response);
    }

    @PostMapping("/logout")
    ResponseEntity<?> logout(HttpServletResponse response) {
        return loginService.logOut(response);
    }
    @GetMapping("/customers")
    public ResponseEntity<?> getAllCustomers(
            @RequestParam(defaultValue = "10")  int pageSize,
            @RequestParam(defaultValue = "0")   int pageOffset,
            @RequestParam(defaultValue = "id")  String sortBy,
            @RequestParam(defaultValue = "asc") String order,
            @RequestParam(required = false)     String email) {
        return adminService.getAllCustomers(pageSize, pageOffset, sortBy, order, email);
    }

    @GetMapping("/sellers")
    public ResponseEntity<?> getAllSellers(
            @RequestParam(defaultValue = "10")  int pageSize,
            @RequestParam(defaultValue = "0")   int pageOffset,
            @RequestParam(defaultValue = "id")  String sortBy,
            @RequestParam(defaultValue = "asc") String order,
            @RequestParam(required = false)     String email) {
        return adminService.getAllSellers(pageSize, pageOffset, sortBy, order, email);
    }

    @PatchMapping("/customer/{id}/activate")
    public ResponseEntity<?> activateCustomer(@PathVariable UUID id) {
        return adminService.activateCustomer(id);
    }

    @PatchMapping("/customer/{id}/deactivate")
    public ResponseEntity<?> deactivateCustomer(@PathVariable UUID id) {
        return adminService.deactivateCustomer(id);
    }

    @PatchMapping("/seller/{id}/activate")
    public ResponseEntity<?> activateSeller(@PathVariable UUID id) {
        return adminService.activateSeller(id);
    }

    @PatchMapping("/seller/{id}/deactivate")
    public ResponseEntity<?> deactivateSeller(@PathVariable UUID id) {
        return adminService.deactivateSeller(id);
    }
}