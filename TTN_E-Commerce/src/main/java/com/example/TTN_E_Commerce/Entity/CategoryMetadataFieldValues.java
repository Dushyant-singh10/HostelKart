package com.example.TTN_E_Commerce.Entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Entity
@Table(name = "category_metadata_field_values",
        uniqueConstraints = @UniqueConstraint(
                columnNames = {"category_id", "category_metadata_field_id"}
        ))
@Getter
@Setter
public class CategoryMetadataFieldValues extends AuditTable{

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne
    @JoinColumn(name = "category_metadata_field_id")
    private CategoryMetadataField field;

    @ManyToOne
    @JoinColumn(name = "category_id")
    private Category category;

    private String fieldValues;

}
