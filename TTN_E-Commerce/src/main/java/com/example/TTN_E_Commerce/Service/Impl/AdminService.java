package com.example.TTN_E_Commerce.Service.Impl;

import com.example.TTN_E_Commerce.DTO.CustomerListDTO;
import com.example.TTN_E_Commerce.DTO.SellerListDTO;
import com.example.TTN_E_Commerce.Entity.Customer;
import com.example.TTN_E_Commerce.Entity.Seller;
import com.example.TTN_E_Commerce.Entity.User;
import com.example.TTN_E_Commerce.Exception.NotFoundException;
import com.example.TTN_E_Commerce.Repository.CustomerRepository;
import com.example.TTN_E_Commerce.Repository.SellerRepository;
import com.example.TTN_E_Commerce.Repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class AdminService {

    @Autowired
    private CustomerRepository customerRepository;
    @Autowired
    private SellerRepository sellerRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private EmailService emailService;

    public ResponseEntity<?> getAllCustomers(int pageSize, int pageOffset,
                                             String sortBy, String order, String email) {
        Sort sort = order.equalsIgnoreCase("desc")
                ? Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(pageOffset, pageSize, sort);
        Page<Customer> page = customerRepository.findAllActive(email, pageable);

        List<CustomerListDTO> list = page.getContent().stream().map(c -> {
            CustomerListDTO dto = new CustomerListDTO();
            dto.setId(c.getUserId());
            dto.setFullName(c.getUser().getFirstName() + " " + c.getUser().getLastName());
            dto.setEmail(c.getUser().getEmail());
            dto.setActive(c.getUser().isActive());
            return dto;
        }).toList();

        return ResponseEntity.ok(buildPageResponse(list, page));
    }

    public ResponseEntity<?> getAllSellers(int pageSize, int pageOffset,
                                           String sortBy, String order, String email) {
        Sort sort = order.equalsIgnoreCase("desc")
                ? Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(pageOffset, pageSize, sort);
        Page<Seller> page = sellerRepository.findAllActive(email, pageable);

        List<SellerListDTO> list = page.getContent().stream().map(s -> {
            SellerListDTO dto = new SellerListDTO();
            dto.setId(s.getUserId());
            dto.setFullName(s.getUser().getFirstName() + " " + s.getUser().getLastName());
            dto.setEmail(s.getUser().getEmail());
            dto.setActive(s.getUser().isActive());
            dto.setCompanyName(s.getCompanyName());
            dto.setCompanyContact(s.getCompanyContact());
            return dto;
        }).toList();

        return ResponseEntity.ok(buildPageResponse(list, page));
    }

    public ResponseEntity<?> activateCustomer(UUID customerId) {
        customerRepository.findById(customerId)
                .orElseThrow(() -> new NotFoundException("Customer not found"));

        User user = userRepository.findById(customerId)
                .orElseThrow(() -> new NotFoundException("Customer not found"));

        if (user.isActive())
            return ResponseEntity.ok("Customer account is already activated");

        user.setActive(true);
        userRepository.save(user);
        emailService.sendHtmlMail(user.getEmail(), "Account Activated",
                "Dear " + user.getFirstName() + ", your account has been activated. You can now login.");
        return ResponseEntity.ok("Customer activated successfully");
    }

    public ResponseEntity<?> deactivateCustomer(UUID customerId) {
        customerRepository.findById(customerId)
                .orElseThrow(() -> new NotFoundException("Customer not found"));

        User user = userRepository.findById(customerId)
                .orElseThrow(() -> new NotFoundException("Customer not found"));

        if (!user.isActive())
            return ResponseEntity.ok("Customer account is already deactivated");

        user.setActive(false);
        userRepository.save(user);
        emailService.sendHtmlMail(user.getEmail(), "Account Deactivated",
                "Dear " + user.getFirstName() + ", your account has been deactivated. Contact support for help.");
        return ResponseEntity.ok("Customer deactivated successfully");
    }

    public ResponseEntity<?> activateSeller(UUID sellerId) {
        sellerRepository.findById(sellerId)
                .orElseThrow(() -> new NotFoundException("Seller not found"));

        User user = userRepository.findById(sellerId)
                .orElseThrow(() -> new NotFoundException("Seller not found"));

        if (user.isActive())
            return ResponseEntity.ok("Seller account is already activated");

        user.setActive(true);
        userRepository.save(user);
        emailService.sendHtmlMail(user.getEmail(), "Account Activated",
                "Dear " + user.getFirstName() + ", your seller account has been activated. You can now login.");
        return ResponseEntity.ok("Seller activated successfully");
    }

    public ResponseEntity<?> deactivateSeller(UUID sellerId) {
        sellerRepository.findById(sellerId)
                .orElseThrow(() -> new NotFoundException("Seller not found"));

        User user = userRepository.findById(sellerId)
                .orElseThrow(() -> new NotFoundException("Seller not found"));

        if (!user.isActive())
            return ResponseEntity.ok("Seller account is already deactivated");

        user.setActive(false);
        userRepository.save(user);
        emailService.sendHtmlMail(user.getEmail(), "Account Deactivated",
                "Dear " + user.getFirstName() + ", your seller account has been deactivated. Contact support for help.");
        return ResponseEntity.ok("Seller deactivated successfully");
    }

    private Map<String, Object> buildPageResponse(List<?> data, Page<?> page) {
        Map<String, Object> response = new HashMap<>();
        response.put("data", data);
        response.put("totalElements", page.getTotalElements());
        response.put("totalPages", page.getTotalPages());
        response.put("currentPage", page.getNumber());
        response.put("pageSize", page.getSize());
        return response;
    }
}