package com.example.TTN_E_Commerce.DTO;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import java.util.UUID;

@Getter @Setter
public class SellerListDTO {
    private UUID id;
    private String fullName;
    private String email;
    private boolean isActive;
    private String companyName;
    String companyContact;
}
