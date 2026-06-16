package com.example.TTN_E_Commerce.Entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;


@Entity
@Setter
@Getter
@Table(name = "category_metadata_field")
public class CategoryMetadataField extends AuditTable{
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(nullable = false)
    private UUID id;

    String name;
}