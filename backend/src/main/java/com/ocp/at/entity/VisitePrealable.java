package com.ocp.at.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Entité représentant une visite préalable sur le terrain (Standard S-HSE-SEC-31 §8.2).
 *
 * <p>La visite est une co-action CEEP (E) + CEEE (P), garantie par HCEE et HMEP.
 * Elle produit obligatoirement :
 * <ul>
 *   <li>Actions de prévention identifiées</li>
 *   <li>Permis requis pour l'intervention</li>
 *   <li>Référence du plan de consignation</li>
 *   <li>AT préreemplie à partir de ces éléments</li>
 * </ul>
 * </p>
 */
@Entity
@Table(name = "visites_prealables")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VisitePrealable {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @CreationTimestamp
    private LocalDateTime dateHeureDebut;

    private LocalDateTime dateHeureFin;

    private Double latitude;

    private Double longitude;

    @Column(columnDefinition = "TEXT")
    private String commentaire;

    /** Indique si la visite a été officiellement finalisée (verrouillée) */
    @Builder.Default
    private boolean effectuee = false;

    // =========================================================================
    // §8.2 - Contenu obligatoire de la visite préalable
    // =========================================================================

    /**
     * Actions de prévention identifiées lors de la visite.
     * Alimentent directement le formulaire AT (Section C/D).
     */
    @Column(name = "actions_prevention_identifiees", columnDefinition = "TEXT")
    private String actionsPreventionIdentifiees;

    /**
     * Permis requis pour l'intervention (ex: permis de feu, espace confiné, fouille, hauteur, consignation).
     * Format JSON ou liste délimitée - alimenté dans TypePermis.
     */
    @Column(name = "permis_requis", columnDefinition = "TEXT")
    private String permisRequis;

    /**
     * Référence du plan de consignation (§8.2 - obligatoire si équipements à consigner).
     * Ex: "PC-2026-0042 - Chaudière Hall B".
     */
    @Column(name = "reference_plan_consignation", length = 255)
    private String referencePlanConsignation;

    /**
     * Décision issue de la visite : les actions de prévention sont-elles suffisantes
     * pour démarrer l'intervention ? (§8.2 - point de décision du logigramme)
     */
    @Column(name = "actions_prevention_suffisantes")
    private Boolean actionsPreventionSuffisantes;

    /**
     * Personne ayant effectué la visite de terrain (CEEP - Exécute, position P).
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "visiteur_id")
    private Utilisateur visiteur;

    /**
     * Participant CEEE à la visite (position E - Participe selon §8.2).
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ceee_participant_id")
    private Utilisateur ceeeParticipant;

    /**
     * Photos prises pendant la visite (stockées sur disque, chemin en DB).
     */
    @OneToMany(mappedBy = "visitePrealable", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<Photo> photos = new ArrayList<>();

    /**
     * Risques identifiés lors de la visite de terrain (pré-identification avant l'analyse formelle).
     */
    @ManyToMany
    @JoinTable(
        name = "visite_prealable_risques",
        joinColumns = @JoinColumn(name = "visite_prealable_id"),
        inverseJoinColumns = @JoinColumn(name = "risque_id")
    )
    @Builder.Default
    private Set<Risque> risquesIdentifies = new HashSet<>();

    /**
     * Analyse des risques formelle créée après finalisation de la visite.
     * Relation bidirectionnelle : AnalyseRisque → VisitePrealable (propriétaire).
     */
    @OneToOne(mappedBy = "visitePrealable", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private AnalyseRisque analyseRisque;
}
