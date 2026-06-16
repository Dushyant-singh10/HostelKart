package com.example.TTN_E_Commerce.Entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Entity
@Table(name = "product_variation")
@Getter
@Setter
public class ProductVariation extends AuditTable {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    private Integer quantityAvailable;

    private Double price;

    @Column(columnDefinition = "json")
    private String metadata;

    private String primaryImageName;

    private Boolean isActive;

    @ManyToOne
    @JoinColumn(name = "product_id")
    private Product product;
}