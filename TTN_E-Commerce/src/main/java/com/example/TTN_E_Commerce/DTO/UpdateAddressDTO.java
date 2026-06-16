package com.example.TTN_E_Commerce.DTO;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class UpdateAddressDTO {


    private String city;
    private String state;
    private String country;
    private String addressLine;
    private String zipCode;
    @Pattern(
            regexp = "^(HOME|OFFICE|OTHER)$",
            message = "Label must be HOME, OFFICE, or OTHER"
    )
    private String label;

}
