package com.example.TTN_E_Commerce.DTO;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.util.UUID;

@Getter @Setter
@NoArgsConstructor
public class CustomerListDTO {
    private UUID id;
    private String fullName;
    private String email;
    private boolean isActive;


}