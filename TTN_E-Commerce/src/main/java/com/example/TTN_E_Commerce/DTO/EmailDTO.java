package com.example.TTN_E_Commerce.DTO;

import com.example.TTN_E_Commerce.Enum.RoleType;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class EmailDTO {
    @Email
    @NotBlank
    String email;

    @NotNull(message = "Role is required (CUSTOMER, SELLER, ADMIN)")
    RoleType role;
}
