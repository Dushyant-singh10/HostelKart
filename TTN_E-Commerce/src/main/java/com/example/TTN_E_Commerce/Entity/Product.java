package com.example.TTN_E_Commerce.Entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Table(name = "product",
        uniqueConstraints = @UniqueConstraint(
                columnNames = {"name", "brand", "seller_user_id"}
        ))
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Product extends AuditTable {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String name;

    private String description;

    @Builder.Default
    private Boolean isCancellable = false;

    @Builder.Default
    private Boolean isReturnable = false;

    @Builder.Default
    private Boolean isActive = false;

    @Builder.Default
    private Boolean isDeleted = false;

    private String brand;

    @ManyToOne
    @JoinColumn(name = "seller_user_id", nullable = false)
    private Seller seller;

    @ManyToOne
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;
}