package com.example.TTN_E_Commerce.Service.Impl;

import com.example.TTN_E_Commerce.DTO.*;
import com.example.TTN_E_Commerce.Entity.*;
import com.example.TTN_E_Commerce.Enum.AddressLabel;
import com.example.TTN_E_Commerce.Enum.RoleType;
import com.example.TTN_E_Commerce.Enum.TokenType;
import com.example.TTN_E_Commerce.Enum.UserAddressLabel;
import java.security.SecureRandom;
import com.example.TTN_E_Commerce.Exception.CustomBadRequestException;
import com.example.TTN_E_Commerce.Exception.CustomUnauthorizedException;
import com.example.TTN_E_Commerce.Exception.NotFoundException;
import com.example.TTN_E_Commerce.Repository.*;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.io.File;
import java.time.LocalDateTime;
import java.util.*;

@Service
public class SellerService{
    @Autowired
    private RoleRepository roleRepository;
    @Autowired
    private SellerRepository sellerRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private EmailService emailService;
    @Autowired
    private ActivationTokenRepository tokenRepository;

    @Transactional
    public ResponseEntity<?> register(SellerDTO sellerDTO) {
        if (!sellerDTO.getPassword().equals(sellerDTO.getConfirmPassword()))
        {
            throw new CustomBadRequestException("Password and Confirm Password didn't match");
        }
        if (sellerRepository.findByUserEmail(sellerDTO.getEmail()).isPresent()) {
            throw new CustomBadRequestException("Email already registered as a Seller.");
        }
        if (sellerRepository.existsByGst(sellerDTO.getGst())) {
            throw new CustomBadRequestException("GST number already registered.");
        }
        if (sellerRepository.existsByCompanyContact(sellerDTO.getCompanyContact())) {
            throw new CustomBadRequestException("Company contact number already registered.");
        }
        if (sellerRepository.existsByCompanyName(sellerDTO.getCompanyName())) {
            throw new CustomBadRequestException("Company name already registered.");
        }

        User user= new User();
        user.setEmail(sellerDTO.getEmail());
        user.setFirstName(sellerDTO.getFirstName());
        user.setMiddleName(sellerDTO.getMiddleName());
        user.setLastName(sellerDTO.getLastName());
        user.setPassword(passwordEncoder.encode(sellerDTO.getPassword()));
        user.setInvalidAttemptCount(0);
        Role role = roleRepository
                .findByAuthority(RoleType.SELLER)
                .orElseThrow(() -> new CustomBadRequestException("Role not found"));

        user.setRoles(Set.of(role));
        user.setActive(false);
        user.setDeleted(false);
        userRepository.save(user);

        Seller seller= new Seller();
        seller.setUser(user);
        seller.setGst(sellerDTO.getGst());
        seller.setCompanyName(sellerDTO.getCompanyName());
        seller.setCompanyContact(sellerDTO.getCompanyContact());
        sellerRepository.save(seller);

        AddressDTO addrDto = sellerDTO.getAddress();
        Address address = new Address();
        address.setCity(addrDto.getCity());
        address.setState(addrDto.getState());
        address.setCountry(addrDto.getCountry());
        address.setAddressLine(addrDto.getAddressLine());
        address.setZipCode(addrDto.getZipCode());
        address.setLabel(AddressLabel.valueOf(addrDto.getLabel().toUpperCase()));
        address.setAddressLabel(UserAddressLabel.SELLER);
        address.setUser(user);
        user.getAddresses().add(address);
        user.setPasswordUpdateDate(LocalDateTime.now());
        userRepository.save(user);

        SecureRandom random = new SecureRandom();
        String otp = String.format("%06d", random.nextInt(900000) + 100000);

        ActivationToken activationToken = new ActivationToken();
        activationToken.setToken(otp);
        activationToken.setUser(user);
        activationToken.setExpiryDate(LocalDateTime.now().plusMinutes(5));
        activationToken.setTokenType(TokenType.SIGNUP);

        tokenRepository.save(activationToken);
        System.out.println("\n==================================================");
        System.out.println("[OTP CODE] Signup OTP for " + sellerDTO.getEmail() + " : " + otp);
        System.out.println("==================================================\n");
        emailService.sendActivationOtpMail(sellerDTO.getEmail(), otp);
        return ResponseEntity.ok(Map.of("message", "Registered successfully. Activation OTP sent to " + sellerDTO.getEmail()));
    }

    public ResponseEntity<?> profile() {
        String email = (String) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Seller seller = sellerRepository.findByUserEmail(email).orElseThrow(() -> new NotFoundException("User Not Found with this EMail"));
        User user = seller.getUser();
        SellerProfileDTO dto = new SellerProfileDTO();
        dto.setId(user.getId());
        dto.setFirstName(user.getFirstName());
        dto.setMiddleName(user.getMiddleName());
        dto.setLastName(user.getLastName());
        dto.setCompanyName(seller.getCompanyName());
        dto.setCompanyContact(seller.getCompanyContact());
        dto.setGst(seller.getGst());
        dto.setActive(user.isActive());

        user.getAddresses().stream()
                .filter(a -> a.getAddressLabel() == UserAddressLabel.SELLER)
                .findFirst()
                .ifPresent(a -> {
                    AddressDTO addrDTO = new AddressDTO();
                    addrDTO.setId(a.getId());
                    addrDTO.setCity(a.getCity());
                    addrDTO.setState(a.getState());
                    addrDTO.setCountry(a.getCountry());
                    addrDTO.setAddressLine(a.getAddressLine());
                    addrDTO.setZipCode(a.getZipCode());
                    addrDTO.setLabel(String.valueOf(a.getLabel()));
                    dto.setAddress(addrDTO);
                });
        File folder = new File("uploads/users/");
        File[] files = folder.listFiles((dir, name) -> name.startsWith(user.getId().toString()));
        if (files != null && files.length > 0) {
            dto.setImage("uploads/users/" + files[0].getName());
        }
        return ResponseEntity.ok(dto);
    }
    @Transactional
    public ResponseEntity<?> updateProfile(SellerUpdateProfileDTO sellerDTO)
    {
        String email= (String) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Seller seller = sellerRepository.findByUserEmail(email).orElseThrow(() -> new NotFoundException("User Not Found with this EMail"));
        User user=seller.getUser();
        if (sellerDTO.getGst() != null) {
            if (!sellerDTO.getGst().equals(seller.getGst()) &&
                    sellerRepository.existsByGst(sellerDTO.getGst())) {
                throw new CustomBadRequestException("GST already registered.");
            }
            seller.setGst(sellerDTO.getGst());
        }
        if (sellerDTO.getCompanyContact() != null) {
            if (!sellerDTO.getCompanyContact().equals(seller.getCompanyContact()) &&
                    sellerRepository.existsByCompanyContact(sellerDTO.getCompanyContact())) {
                throw new CustomBadRequestException("Company contact already registered.");
            }
            seller.setCompanyContact(sellerDTO.getCompanyContact());
        }

        if (sellerDTO.getCompanyName() != null) {
            if (!sellerDTO.getCompanyName().equals(seller.getCompanyName()) &&
                    sellerRepository.existsByCompanyName(sellerDTO.getCompanyName())) {
                throw new CustomBadRequestException("Company name already registered.");
            }
            seller.setCompanyName(sellerDTO.getCompanyName());
        }
        if (sellerDTO.getFirstName() != null)  user.setFirstName(sellerDTO.getFirstName());
        if (sellerDTO.getMiddleName() != null) user.setMiddleName(sellerDTO.getMiddleName());
        if (sellerDTO.getLastName() != null)   user.setLastName(sellerDTO.getLastName());
        userRepository.save(user);

        sellerRepository.save(seller);

        return ResponseEntity.ok("Profile Updated");
    }
    @Transactional
    public ResponseEntity<?> updatePassword(ChangePasswordDTO dto)
    {

        String email= (String) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Seller seller = sellerRepository.findByUserEmail(email).orElseThrow(() -> new NotFoundException("User Not Found with this EMail"));

        if(!passwordEncoder.matches(dto.getPrevPassword(), seller.getUser().getPassword())) throw new CustomUnauthorizedException("Current Password is Wrong");
        if (!dto.getPassword().equals(dto.getConfirmPAssword())) throw new CustomBadRequestException("New Password Mismatches");

        User user=seller.getUser();
        if (passwordEncoder.matches(dto.getPassword(), user.getPassword())) {
            throw new CustomBadRequestException("New password cannot be same as old password.");
        }
        user.setPassword(passwordEncoder.encode(dto.getPassword()));
        user.setPasswordUpdateDate(LocalDateTime.now());
        userRepository.save(user);

        emailService.sendHtmlMail(user.getEmail(),"Password Updated","Your password has been updated");

        return ResponseEntity.ok("Password Updated");
    }

//    @Transactional
//    public ResponseEntity<?> addAddress(AddressDTO dto) {
//        String email = (String) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
//        Seller seller = sellerRepository.findByUserEmail(email)
//                .orElseThrow(() -> new NotFoundException("Seller not found"));
//        User user = seller.getUser();
//
//        // Seller ka sirf ek address allowed
//        boolean alreadyExists = user.getAddresses().stream()
//                .anyMatch(a -> a.getAddressLabel() == UserAddressLabel.SELLER);
//        if (alreadyExists)
//            throw new CustomBadRequestException("Seller can only have one address. Use update address instead.");
//
//        Address addr = new Address();
//        addr.setAddressLine(dto.getAddressLine());
//        addr.setCity(dto.getCity());
//        addr.setState(dto.getState());
//        addr.setCountry(dto.getCountry());
//        addr.setZipCode(dto.getZipCode());
//        addr.setLabel(AddressLabel.valueOf(dto.getLabel().toUpperCase()));
//        addr.setAddressLabel(UserAddressLabel.SELLER);
//        addr.setUser(user);
//
//        user.getAddresses().add(addr);
//        userRepository.save(user);
//        return ResponseEntity.ok("Address added successfully");
//    }


    @Transactional
    public ResponseEntity<?> updateAddress(UpdateAddressDTO dto, UUID id) {

        String email = (String) SecurityContextHolder.getContext()
                .getAuthentication().getPrincipal();

        Seller seller = sellerRepository.findByUserEmail(email)
                .orElseThrow(() -> new NotFoundException("Seller not found"));

        User user = seller.getUser();

        Address mainAddress = user.getAddresses().stream()
                .filter(addr -> addr.getId().equals(id) && addr.getAddressLabel() == UserAddressLabel.SELLER)
                .findFirst()
                .orElseThrow(() -> new NotFoundException("Address not found with id: " + id));

        if (dto.getAddressLine() != null) mainAddress.setAddressLine(dto.getAddressLine());
        if (dto.getCity()        != null) mainAddress.setCity(dto.getCity());
        if (dto.getState()       != null) mainAddress.setState(dto.getState());
        if (dto.getCountry()     != null) mainAddress.setCountry(dto.getCountry());
        if (dto.getLabel() != null) {
            try {
                mainAddress.setLabel(AddressLabel.valueOf(dto.getLabel().toUpperCase()));
            } catch (IllegalArgumentException e) {
                throw new CustomBadRequestException(
                        "Invalid label. Allowed values: HOME, OFFICE, OTHER"
                );
            }
        }
        if (dto.getZipCode()     != null) mainAddress.setZipCode(dto.getZipCode());

        userRepository.save(user);

        return ResponseEntity.ok("Address updated successfully");
    }


}