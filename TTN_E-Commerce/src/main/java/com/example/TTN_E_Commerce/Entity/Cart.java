package com.example.TTN_E_Commerce.Entity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Entity
@Table(name = "cart")
@Getter @Setter
public class Cart extends AuditTable{

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    private Integer quantity;

    @Column(nullable = false)
    private Boolean isWishlistItem = false;

    @ManyToOne(optional = false)
    @JoinColumn(name = "customer_user_id", nullable = false)
    private Customer customer;

    @ManyToOne(optional = false)
    @JoinColumn(name = "product_variation_id", nullable = false)
    private ProductVariation productVariation;
}
