package com.example.TTN_E_Commerce.Entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Entity
@Setter
@Getter
@Table(name = "category")
public class Category extends AuditTable{
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(nullable = false)
    private UUID id;

    private String name;

    @ManyToOne
    @JoinColumn(name = "parent_catogory_id")
    Category parentCategory;

}
