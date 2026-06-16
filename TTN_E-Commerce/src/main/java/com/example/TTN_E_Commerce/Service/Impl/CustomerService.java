package com.example.TTN_E_Commerce.Service.Impl;

import com.example.TTN_E_Commerce.DTO.*;
import com.example.TTN_E_Commerce.Entity.*;
import com.example.TTN_E_Commerce.Enum.AddressLabel;
import com.example.TTN_E_Commerce.Enum.RoleType;
import com.example.TTN_E_Commerce.Enum.UserAddressLabel;
import com.example.TTN_E_Commerce.Exception.CustomBadRequestException;
import com.example.TTN_E_Commerce.Exception.NotFoundException;
import com.example.TTN_E_Commerce.Repository.*;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.io.File;
import java.time.LocalDateTime;
import java.util.*;

@Service
public class CustomerService {
    @Autowired
    private RoleRepository roleRepository;
    @Autowired
    private CustomerRepository customerRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private EmailService emailService;
    @Autowired
    private ActivationTokenRepository tokenRepository;

    @Autowired
    private AddressRepository addressRepository;

    @Autowired
    private MessageSource messageSource;

    private String msg(String key) {
        return messageSource.getMessage(key, null, LocaleContextHolder.getLocale());
    }

    public ResponseEntity<?> register(CustomerDTO customerDTO) {
        if (!customerDTO.getPassword().equals(customerDTO.getConfirmPassword()))
        {
            throw new CustomBadRequestException(msg("error.password.mismatch"));
        }
        if (customerRepository.findByUserEmail(customerDTO.getEmail()).isPresent()) {
            throw new CustomBadRequestException(msg("error.email.already.customer"));
        }

        User user= new User();
        user.setEmail(customerDTO.getEmail());
        user.setFirstName(customerDTO.getFirstName());
        user.setMiddleName(customerDTO.getMiddleName());
        user.setLastName(customerDTO.getLastName());
        user.setPassword(passwordEncoder.encode(customerDTO.getPassword()));
        user.setInvalidAttemptCount(0);
        Role role = roleRepository
                .findByAuthority(RoleType.CUSTOMER)
                .orElseThrow(() -> new RuntimeException("Role not found"));

        user.setRoles(Set.of(role));
        user.setActive(false);
        user.setDeleted(false);
        user.setPasswordUpdateDate(LocalDateTime.now());
        userRepository.save(user);

        Customer customer = new Customer();
        customer.setUser(user);
        customer.setContact(customerDTO.getContact());
        customerRepository.save(customer);

        String token = UUID.randomUUID().toString();
        ActivationToken activationToken = new ActivationToken();
        activationToken.setToken(token);
        activationToken.setUser(user);
        activationToken.setExpiryDate(LocalDateTime.now().plusHours(24));

        tokenRepository.save(activationToken);
        emailService.sendActivationMail(customerDTO.getEmail(),token);
        return ResponseEntity.ok().body("Registered successfully with Token : "+ token);
    }
    public ResponseEntity<?> profile() {
        String email = (String) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Customer customer = customerRepository.findByUserEmail(email).orElseThrow(() -> new NotFoundException("User Not Found with this EMail"));
        User user = customer.getUser();
        CustomerProfileDTO dto = new CustomerProfileDTO();
        dto.setId(user.getId());
        dto.setFirstName(user.getFirstName());
        dto.setMiddleName(user.getMiddleName());
        dto.setLastName(user.getLastName());
        dto.setContact(customer.getContact());
        dto.setActive(user.isActive());

        File folder = new File("uploads/users/");
        File[] files = folder.listFiles((dir, name) -> name.startsWith(user.getId().toString()));
        if (files != null && files.length > 0) {
            dto.setImage("uploads/users/" + files[0].getName());
        }
        return ResponseEntity.ok(dto);
    }

    public ResponseEntity<?> getAddresses() {
        String email = (String) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Customer customer = customerRepository.findByUserEmail(email)
                .orElseThrow(() -> new NotFoundException("Customer not found"));
        User user = customer.getUser();

        List<Address> addresses = addressRepository.findByUserAndAddressLabel(user, UserAddressLabel.CUSTOMER);

        List<AddressDTO> addressDTOs = addresses.stream().map(addr -> {
            AddressDTO dto = new AddressDTO();
            dto.setId(addr.getId());
            dto.setCity(addr.getCity());
            dto.setState(addr.getState());
            dto.setCountry(addr.getCountry());
            dto.setAddressLine(addr.getAddressLine());
            dto.setZipCode(addr.getZipCode());
            dto.setLabel(addr.getLabel() != null ? addr.getLabel().name() : null);
            return dto;
        }).toList();

        return ResponseEntity.ok(addressDTOs);
    }

    @Transactional
    public ResponseEntity<?> updateProfile(CustomerUpdateprofileDTO dto) {
        String email = (String) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Customer customer = customerRepository.findByUserEmail(email)
                .orElseThrow(() -> new NotFoundException("Customer not found"));
        User user = customer.getUser();

        if (dto.getFirstName() != null)  user.setFirstName(dto.getFirstName());
        if (dto.getMiddleName() != null) user.setMiddleName(dto.getMiddleName());
        if (dto.getLastName() != null)   user.setLastName(dto.getLastName());
        if (dto.getContact() != null)    customer.setContact(dto.getContact());

        userRepository.save(user);
        customerRepository.save(customer);

        return ResponseEntity.ok(msg("success.profile.updated"));
    }
    @Transactional
    public ResponseEntity<?> updatePassword(ChangePasswordDTO dto) {
        String email = (String) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Customer customer = customerRepository.findByUserEmail(email)
                .orElseThrow(() -> new NotFoundException("Customer not found"));
        User user = customer.getUser();

        if (!passwordEncoder.matches(dto.getPrevPassword(), user.getPassword())) {
            throw new CustomBadRequestException(msg("error.current.password.incorrect"));
        }

        if (!dto.getPassword().equals(dto.getConfirmPAssword())) {
            throw new CustomBadRequestException(msg("error.new.password.mismatch"));
        }

        if (passwordEncoder.matches(dto.getPassword(), user.getPassword())) {
            throw new CustomBadRequestException(msg("error.new.password.same"));
        }

        user.setPassword(passwordEncoder.encode(dto.getPassword()));
        user.setPasswordUpdateDate(java.time.LocalDateTime.now());
        userRepository.save(user);

        emailService.sendHtmlMail(
                user.getEmail(),
                "Password Changed",
                "Your password has been changed successfully. If this wasn't you, contact support immediately."
        );

        return ResponseEntity.ok("Password updated successfully");
    }

    @Transactional
    public ResponseEntity<?> addAddress(AddressDTO dto) {
        String email = (String) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Customer customer = customerRepository.findByUserEmail(email)
                .orElseThrow(() -> new NotFoundException("Customer not found"));
        User user = customer.getUser();

        Address address = new Address();
        address.setCity(dto.getCity());
        address.setState(dto.getState());
        address.setCountry(dto.getCountry());
        address.setAddressLine(dto.getAddressLine());
        address.setZipCode(dto.getZipCode());
        address.setLabel(AddressLabel.valueOf(dto.getLabel()));
        address.setAddressLabel(UserAddressLabel.CUSTOMER);
        address.setUser(user);

        addressRepository.save(address);

        return ResponseEntity.ok(msg("success.address.added"));
    }

    @Transactional
    public ResponseEntity<?> deleteAddress(UUID addressId) {
        String email = (String) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Customer customer = customerRepository.findByUserEmail(email)
                .orElseThrow(() -> new NotFoundException("Customer not found"));
        User user = customer.getUser();

        Address address = addressRepository.findById(addressId)
                .orElseThrow(() -> new NotFoundException("Address not found"));

        if (!address.getUser().getId().equals(user.getId())) {
            throw new CustomBadRequestException(msg("error.address.delete.unauthorized"));
        }

        addressRepository.delete(address);
        return ResponseEntity.ok(msg("success.address.deleted"));
    }
    public ResponseEntity<?> updateAddress(UUID addressId, UpdateAddressDTO dto) {
        String email = (String) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Customer customer = customerRepository.findByUserEmail(email)
                .orElseThrow(() -> new NotFoundException("Customer not found"));
        User user = customer.getUser();

        Address address = addressRepository.findById(addressId)
                .orElseThrow(() -> new NotFoundException("Address not found"));

        // Ye address is customer ka hi hai?
        if (!address.getUser().getId().equals(user.getId())) {
            throw new CustomBadRequestException(msg("error.address.update.unauthorized"));
        }

        // Sirf jo fields aaye woh update
        if (dto.getCity() != null)        address.setCity(dto.getCity());
        if (dto.getState() != null)       address.setState(dto.getState());
        if (dto.getCountry() != null)     address.setCountry(dto.getCountry());
        if (dto.getAddressLine() != null) address.setAddressLine(dto.getAddressLine());
        if (dto.getZipCode() != null)     address.setZipCode(dto.getZipCode());
        if (dto.getLabel() != null)       address.setLabel(AddressLabel.valueOf(dto.getLabel()));

        addressRepository.save(address);
        return ResponseEntity.ok(msg("success.address.updated"));
    }

}
