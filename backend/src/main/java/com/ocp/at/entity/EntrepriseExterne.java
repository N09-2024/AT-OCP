package com.ocp.at.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "entreprises_externes")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EntrepriseExterne {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(nullable = false)
    private String nomEntreprise;

    private String adresse;

    private String telephone;

    private String responsable;
}
