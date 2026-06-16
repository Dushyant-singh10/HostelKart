package com.example.TTN_E_Commerce.Controller;

import com.example.TTN_E_Commerce.DTO.ResetPasswordDTO;
import com.example.TTN_E_Commerce.Enum.RoleType;
import com.example.TTN_E_Commerce.Service.Impl.ForgotPasswordService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/reset-password")
public class ForgotPasswordController {

    @Autowired
    private ForgotPasswordService service;

    @PostMapping("/newPassword")
    public ResponseEntity<?> resetPassword(@RequestParam String email,
                                           @RequestParam RoleType role) {
        return service.receiveTokenBasedMail(email, role);
    }

    @PatchMapping("/changePassword")
    public ResponseEntity<?> changePassword(@RequestParam String token,
                                            @RequestBody ResetPasswordDTO dto) {
        return service.changePassword(dto, token);
    }
}