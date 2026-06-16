package com.example.TTN_E_Commerce.DTO;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SellerDTO {
    @Email
    @NotBlank
    private String email;

    @NotBlank
    @Pattern(
            regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[^A-Za-z\\d]).{8,15}$",
            message = "Password must be 8-15 chars with upper, lower, number & special char"
    )
    private String password;

    @NotBlank
    private String confirmPassword;

    @NotBlank
    private String firstName;

    private String middleName;

    @NotBlank
    private String lastName;

    @NotBlank(message = "GST cannot be empty")
    @Pattern(
            regexp = "^[0-9]{2}[A-Z]{5}[0-9]{4}[A-Z]{1}[A-Z0-9]{3}$",
            message = "Invalid GST number"
    )
    private String gst;

    @NotBlank(message = "Company contact is required")
    @Pattern(
            regexp = "^[6-9]\\d{9}$",
            message = "Invalid phone number"
    )
    private String companyContact;

    @NotBlank(message = "Company name cannot be empty")
    @Size(min = 3, max = 50)
    private String companyName;

    private AddressDTO address;

}
