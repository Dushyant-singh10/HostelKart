package com.example.TTN_E_Commerce.DTO;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class SellerUpdateProfileDTO {
    UUID id;

    private String firstName;
    private String middleName;
    private String lastName;
    @Pattern(
            regexp = "^[6-9]\\d{9}$",
            message = "Invalid phone number"
    )
    private String companyContact;
    private String companyName;
    @Pattern(
            regexp = "^[0-9]{2}[A-Z]{5}[0-9]{4}[A-Z]{1}[A-Z0-9]{3}$",
            message = "Invalid GST number"
    )
    private String gst;
}
