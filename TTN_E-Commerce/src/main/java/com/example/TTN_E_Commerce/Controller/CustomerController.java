package com.example.TTN_E_Commerce.Controller;

import com.example.TTN_E_Commerce.DTO.*;
import com.example.TTN_E_Commerce.Enum.RoleType;
import com.example.TTN_E_Commerce.Service.Impl.CustomerService;
import com.example.TTN_E_Commerce.Service.Impl.ImageService;
import com.example.TTN_E_Commerce.Service.Impl.LoginService;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.UUID;

@RestController
@RequestMapping("/customer")
public class CustomerController {
    @Autowired
    private CustomerService service;
    @Autowired
    private ImageService imageService;
    @Autowired
    private LoginService loginService;

    @PostMapping("/register")
    ResponseEntity<?> register(@Valid @RequestBody CustomerDTO customerDTO) {
        return service.register(customerDTO);
    }

    @PostMapping("/login")
    ResponseEntity<?> login(@Valid @RequestBody LoginDTO loginDTO, HttpServletResponse response) {
        return loginService.login(loginDTO, RoleType.CUSTOMER, response);
    }

    @PostMapping("/logout")
    ResponseEntity<?> logout(HttpServletResponse response) {
        return loginService.logOut(response);
    }
    @GetMapping("/profile")
    public ResponseEntity<?> profile(){

        return service.profile();
    }
    @PostMapping("/profile/image")
    public ResponseEntity<?> uploadImage(
            @RequestParam("image") MultipartFile image) throws IOException {

        return imageService.uploadProfileImage(image);
    }
    @GetMapping("/profile/image")
    public ResponseEntity<?> getImage() throws IOException {

        return imageService.getImage();
    }

    @GetMapping("/addresses")
    public ResponseEntity<?> getAddresses() {
        return service.getAddresses();
    }

    @PatchMapping("/profile")
    public ResponseEntity<?> updateProfile(@Valid @RequestBody CustomerUpdateprofileDTO dto) {
        return service.updateProfile(dto);
    }
    @PatchMapping("/updatePassword")
    public ResponseEntity<?> updatePassword(@Valid @RequestBody ChangePasswordDTO dto) {
        return service.updatePassword(dto);
    }

    @PostMapping("/address")
    public ResponseEntity<?> addAddress(@Valid @RequestBody AddressDTO dto) {
        return service.addAddress(dto);
    }

    @DeleteMapping("/address/{addressId}")
    public ResponseEntity<?> deleteAddress(@PathVariable UUID addressId) {
        return service.deleteAddress(addressId);
    }
    @PatchMapping("/address/{addressId}")
    public ResponseEntity<?> updateAddress(@PathVariable UUID addressId,
                                           @Valid @RequestBody UpdateAddressDTO dto) {
        return service.updateAddress(addressId, dto);
    }



}
