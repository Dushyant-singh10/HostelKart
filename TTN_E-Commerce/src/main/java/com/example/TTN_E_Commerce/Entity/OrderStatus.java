package com.example.TTN_E_Commerce.Entity;
import com.example.TTN_E_Commerce.Enum.OrderState;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "order_status")
@Getter @Setter
public class OrderStatus extends AuditTable{

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Enumerated(EnumType.STRING)
    private OrderState fromStatus;

    @Enumerated(EnumType.STRING)
    private OrderState toStatus;

    private String transitionNotesComments;

    private LocalDateTime transitionDate;

    @ManyToOne(optional = false)
    @JoinColumn(name = "order_product_id", nullable = false)
    private OrderProduct orderProduct;

}