package com.ocp.at.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

/**
 * Représente un essai réalisé lors de la réception des travaux.
 */
@Entity
@Table(name = "essais")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString
public class Essai {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @EqualsAndHashCode.Include
    private String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reception_id", nullable = false)
    @ToString.Exclude
    private ReceptionTravaux receptionTravaux;

    @Column(nullable = false)
    private String nom;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(length = 500)
    private String resultat;

    @Column(nullable = false)
    @Builder.Default
    private Boolean conforme = false;

    @Column(columnDefinition = "TEXT")
    private String commentaire;
}
