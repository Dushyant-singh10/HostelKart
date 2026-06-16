package com.example.TTN_E_Commerce.Controller;

import com.example.TTN_E_Commerce.DTO.LoginDTO;
import com.example.TTN_E_Commerce.Enum.RoleType;
import com.example.TTN_E_Commerce.Service.Impl.LoginService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/user")
public class UserController {

    @Autowired
    private LoginService loginService;

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginDTO dto,
                                   HttpServletResponse response, @RequestParam RoleType role) {
        return loginService.login(dto, role, response);
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpServletResponse response) {
        return loginService.logOut(response);
    }
    @PutMapping("/refreshAccessToken")
    ResponseEntity<?> refreshAccessToken(HttpServletRequest request) {
        return loginService.refreshAccessToken(request);
    }
}
