package com.example.TTN_E_Commerce.Entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Entity
@Getter
@Setter
@Table(name = "seller")
public class Seller extends AuditTable{

    @Id
    private UUID userId;

    @MapsId
    @OneToOne
    @JoinColumn(name = "user_id")
    private User user;

    private String gst;
    private String companyContact;
    private String companyName;

}
