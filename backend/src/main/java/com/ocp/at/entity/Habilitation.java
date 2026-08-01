package com.ocp.at.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Entité représentant l'habilitation officielle d'un agent à délivrer des Autorisations de Travail.
 * Formulaire F-HSE-SEC-31-02 — Standard OCP S-HSE-SEC-31 §9.
 */
@Entity
@Table(name = "habilitations")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Habilitation {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    /** Agent habilité à délivrer des AT */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "utilisateur_id", nullable = false)
    private Utilisateur utilisateur;

    /** HCEP qui a signé la désignation (§9 — responsable propriétaire) */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "designe_par_id")
    private Utilisateur designePar;

    @Column(name = "date_habilitation", nullable = false)
    private LocalDate dateHabilitation;

    /** Revue annuelle obligatoire (§9 : liste revue à chaque changement organisationnel et au moins une fois par an) */
    @Column(name = "valide_jusqu_au", nullable = false)
    private LocalDate valideJusquAu;

    @Column(nullable = false)
    private Boolean actif;

    @Column(columnDefinition = "TEXT")
    private String observations;

    @CreationTimestamp
    @Column(name = "date_creation")
    private LocalDateTime dateCreation;
}
