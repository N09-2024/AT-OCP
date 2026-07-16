package com.ocp.at.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "types_permis")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TypePermis {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(nullable = false, unique = true)
    private String nom;

    private String description;
}
