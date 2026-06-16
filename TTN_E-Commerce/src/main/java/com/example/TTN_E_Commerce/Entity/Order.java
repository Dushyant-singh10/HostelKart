package com.example.TTN_E_Commerce.Entity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "orders")
@Getter @Setter
public class Order extends AuditTable{

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    private Double amountPaid;

    private LocalDateTime dateCreated;

    private String paymentMethod;

    // Snapshot address (Important 🔥)
    private String customerAddressCity;
    private String customerAddressState;
    private String customerAddressCountry;
    private String customerAddressAddressLine;
    private String customerAddressZipCode;
    private String customerAddressLabel;

    // Customer FK
    @ManyToOne(optional = false)
    @JoinColumn(name = "customer_user_id", nullable = false)
    private Customer customer;

    // One order → many order items
    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL)
    private List<OrderProduct> orderProducts;

}