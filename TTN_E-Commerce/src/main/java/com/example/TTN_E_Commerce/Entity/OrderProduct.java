package com.example.TTN_E_Commerce.Entity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "order_product")
@Getter @Setter
public class OrderProduct extends AuditTable{

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    private Integer quantity;

    private Double price; // price snapshot

    // Order FK
    @ManyToOne(optional = false)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    // Product Variation FK
    @ManyToOne(optional = false)
    @JoinColumn(name = "product_variation_id", nullable = false)
    private ProductVariation productVariation;

    // One order item → multiple status transitions
    @OneToMany(mappedBy = "orderProduct", cascade = CascadeType.ALL)
    private List<OrderStatus> orderStatusList;

}