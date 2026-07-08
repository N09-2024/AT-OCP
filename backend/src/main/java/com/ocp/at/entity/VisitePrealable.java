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
 * Entité représentant une visite préalable sur le terrain.
 * La visite est toujours liée à un document source (DI, OT ou BT),
 * et non directement à l'AT.
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

    /**
     * Personne ayant effectué la visite de terrain.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "visiteur_id")
    private Utilisateur visiteur;

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
