package com.example.TTN_E_Commerce.DTO;

import com.example.TTN_E_Commerce.Entity.Address;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import jakarta.validation.constraints.Pattern;

import java.util.List;
import java.util.UUID;

@Getter
@Setter
public class SellerProfileDTO {
    UUID id;

    @Size(min = 2, max = 30, message = "First name must be 2-30 characters")
    private String firstName;

    private String middleName;

    @Size(min = 2, max = 30, message = "Last name must be 2-30 characters")
    private String lastName;
    private boolean isActive;
    @Pattern(
            regexp = "^[6-9]\\d{9}$",
            message = "Invalid phone number"
    )
    private String companyContact;

    @Size(min = 3, max = 50, message = "Company name must be 3-50 characters")
    private String companyName;

    @Pattern(
            regexp = "^[0-9]{2}[A-Z]{5}[0-9]{4}[A-Z]{1}[A-Z0-9]{3}$",
            message = "Invalid GST number"
    )
    private String gst;
    private AddressDTO address;
    private String image;

}
