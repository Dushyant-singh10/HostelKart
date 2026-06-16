package com.example.TTN_E_Commerce.Controller;

import com.example.TTN_E_Commerce.DTO.*;
import com.example.TTN_E_Commerce.Enum.RoleType;
import com.example.TTN_E_Commerce.Repository.UserRepository;
import com.example.TTN_E_Commerce.Service.Impl.ImageService;
import com.example.TTN_E_Commerce.Service.Impl.LoginService;
import com.example.TTN_E_Commerce.Service.Impl.SellerService;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.UUID;

@RestController
@RequestMapping("/seller")
public class SellerController {

    @Autowired
    private SellerService service;

    @Autowired
    private ImageService imageService;

    @Autowired
    private LoginService loginService;


    @Autowired
    private UserRepository userRepository;
    @PostMapping("/register")
    ResponseEntity<?> register(@Valid @RequestBody SellerDTO sellerDTO)
    {
        return service.register(sellerDTO);
    }
    @PostMapping("/login")
    ResponseEntity<?> login(@Valid @RequestBody LoginDTO loginDTO, HttpServletResponse response) {
        return loginService.login(loginDTO, RoleType.SELLER, response);
    }

    @PostMapping("/logout")
    ResponseEntity<?> logout(HttpServletResponse response) {
        return loginService.logOut(response);
    }

    @GetMapping("/profile")
    ResponseEntity<?> getProfile() {
        return service.profile();
    }


    @PatchMapping("/profile")
    ResponseEntity<?> updateProfile(@Valid @RequestBody SellerUpdateProfileDTO sellerDTO) {
        return service.updateProfile(sellerDTO);
    }

    @PatchMapping("/password")
    ResponseEntity<?> updatePassword(@Valid @RequestBody ChangePasswordDTO dto) {
        return service.updatePassword(dto);
    }
//
//    @PostMapping("/address")
//    ResponseEntity<?> addAddress(@Valid @RequestBody AddressDTO dto) {
//        return service.addAddress(dto);
//    }

    @PostMapping("/profile/image")
    public ResponseEntity<?> uploadImage(
            @RequestParam("image") MultipartFile image) throws IOException {

        return imageService.uploadProfileImage(image);
    }
    @GetMapping("/profile/image")
    public ResponseEntity<?> getImage() throws IOException {

        return imageService.getImage();
    }

    @PutMapping("/changeAddress/{id}")
    ResponseEntity<?> updateAddress(@Valid @RequestBody UpdateAddressDTO dto, @PathVariable UUID id ){
        return service.updateAddress(dto,id);
    }
}
