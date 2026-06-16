package com.example.TTN_E_Commerce.Controller;

import com.example.TTN_E_Commerce.DTO.EmailDTO;
import com.example.TTN_E_Commerce.Service.Impl.ActivationTokenService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
public class ActivationTokenController {
    @Autowired
    ActivationTokenService service;
    @PutMapping("/activate")
    ResponseEntity<?> activate(@RequestParam String token) {
        return service.activate(token);
    }

    @PostMapping("/resendToken")
    ResponseEntity<?> resendToken(@Valid @RequestBody EmailDTO dto) {
        return service.resendToken(dto.getEmail(), dto.getRole());
    }
}
