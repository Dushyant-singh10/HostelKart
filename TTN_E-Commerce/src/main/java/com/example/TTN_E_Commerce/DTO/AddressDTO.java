package com.example.TTN_E_Commerce.DTO;

import com.example.TTN_E_Commerce.Entity.Address;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class AddressDTO {
    private UUID id;
    @NotBlank
    private String city;
    @NotBlank
    private String state;
    @NotBlank
    private String country;
    @NotBlank(message = "AddressLine is missing")
    private String addressLine;
    @NotBlank
    private String zipCode;
    @NotBlank
    @Pattern(
            regexp = "^(HOME|OFFICE|OTHER)$",
            message = "Label must be HOME, OFFICE, or OTHER"
    )
    private String label;
}
