package com.example.TTN_E_Commerce.Entity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Entity
@Table(name = "product_review")
@Getter @Setter
public class ProductReview extends AuditTable{

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    private String review;

    private Integer rating;

    @ManyToOne
    @JoinColumn(name = "customer_user_id")
    private Customer customer;

    @ManyToOne
    @JoinColumn(name = "product_id")
    private Product product;

}