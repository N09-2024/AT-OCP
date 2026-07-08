package com.ocp.at.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

/**
 * Entité représentant l'analyse formelle des risques liée à une visite préalable.
 * L'AT accède à l'analyse via : document → visite → analyse.
 */
@Entity
@Table(name = "analyses_risques")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AnalyseRisque {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @CreationTimestamp
    private LocalDateTime dateAnalyse;

    @Column(columnDefinition = "TEXT")
    private String commentaire;

    /**
     * Lien vers la VisitePrealable (propriétaire de la relation).
     * Une analyse ne peut exister que si la visite est effectuée.
     */
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "visite_prealable_id", unique = true)
    private VisitePrealable visitePrealable;

    /**
     * Analyste ayant réalisé l'analyse formelle des risques.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "analyseur_id")
    private Utilisateur analyseur;

    @ManyToMany
    @JoinTable(
        name = "analyse_risque_risques",
        joinColumns = @JoinColumn(name = "analyse_risque_id"),
        inverseJoinColumns = @JoinColumn(name = "risque_id")
    )
    @Builder.Default
    private Set<Risque> risques = new HashSet<>();

    @ManyToMany
    @JoinTable(
        name = "analyse_risque_mesures",
        joinColumns = @JoinColumn(name = "analyse_risque_id"),
        inverseJoinColumns = @JoinColumn(name = "mesure_id")
    )
    @Builder.Default
    private Set<MesurePreparation> mesures = new HashSet<>();

    @ManyToMany
    @JoinTable(
        name = "analyse_risque_epis",
        joinColumns = @JoinColumn(name = "analyse_risque_id"),
        inverseJoinColumns = @JoinColumn(name = "epi_id")
    )
    @Builder.Default
    private Set<EPI> epis = new HashSet<>();

    @ManyToMany
    @JoinTable(
        name = "analyse_risque_moyens_acces",
        joinColumns = @JoinColumn(name = "analyse_risque_id"),
        inverseJoinColumns = @JoinColumn(name = "moyen_id")
    )
    @Builder.Default
    private Set<MoyenAcces> moyensAcces = new HashSet<>();
}
