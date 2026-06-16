package com.example.TTN_E_Commerce.DTO;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.UUID;

@Getter
@Setter
public class CustomerProfileDTO {
        UUID id;

        @Size(min = 2, max = 30, message = "First name must be 2-30 characters")
        private String firstName;

        private String middleName;

        @Size(min = 2, max = 30, message = "Last name must be 2-30 characters")
        private String lastName;

        @NotBlank
        private String contact;

        private String image;

        private boolean isActive;



}
