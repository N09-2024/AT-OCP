package com.ocp.at.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

/**
 * Entité représentant la classification préalable d'une intervention par le HCEP (§6 Standard OCP S-HSE-SEC-31).
 * Détermine si une Autorisation de Travail est obligatoire (Niveau 2) ou non (Niveau 1).
 */
@Entity
@Table(name = "classifications_interventions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ClassificationIntervention {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    /** Référence unique de la classification (ex: CLF-2024-001) */
    @Column(nullable = false, unique = true)
    private String reference;

    /**
     * NIVEAU_1 : Intervention de routine / interne — Pas d'AT requise (§7.1)
     * NIVEAU_2 : Intervention à risque / tiers — AT obligatoire (§7.2)
     */
    @Column(nullable = false)
    private String niveau;

    /** Vrai si l'intervenant est un tiers/entreprise extérieure → Niveau 2 forcé (§8.1 NB) */
    @Column(name = "est_tiers", nullable = false)
    private Boolean estTiers;

    @Column(name = "nature_intervention", columnDefinition = "TEXT")
    private String natureIntervention;

    /** Zone de l'intervention */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "zone_id")
    private Zone zone;

    /** Service propriétaire de l'installation */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "service_id")
    private Service service;

    /** HCEP ayant effectué la classification */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "classifie_par_id")
    private Utilisateur classifiePar;

    @Column(name = "date_classification", nullable = false)
    @CreationTimestamp
    private LocalDateTime dateClassification;

    @Column(columnDefinition = "TEXT")
    private String observations;

    /** Statut : EFFECTUEE → NIVEAU_2_AT_CREEE (si niveau 2) */
    @Column(nullable = false)
    private String statut;

    /** AT liée créée si Niveau 2 */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "autorisation_travail_id")
    private AutorisationTravail autorisationTravail;
}
