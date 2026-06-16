package com.example.TTN_E_Commerce.DTO;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class CustomerUpdateprofileDTO {
    UUID id;

    private String firstName;

    private String middleName;

    private String lastName;

    private String contact;


}
