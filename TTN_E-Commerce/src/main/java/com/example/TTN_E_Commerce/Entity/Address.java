package com.example.TTN_E_Commerce.Entity;

import com.example.TTN_E_Commerce.Enum.AddressLabel;
import com.example.TTN_E_Commerce.Enum.UserAddressLabel;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Entity
@Table(name = "address")
@Getter
@Setter
public class Address extends AuditTable{

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    private String city;
    private String state;
    private String country;
    private String addressLine;
    private String zipCode;
    @Enumerated(EnumType.STRING)
    private AddressLabel label;
    @Enumerated(EnumType.STRING)
    private UserAddressLabel addressLabel;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;
}