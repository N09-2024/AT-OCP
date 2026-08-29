package com.ocp.at.entity;

import com.ocp.at.entity.enums.StatutReconduction;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "reconductions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString
public class Reconduction {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @EqualsAndHashCode.Include
    private String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "autorisation_travail_id", nullable = false)
    @ToString.Exclude
    private AutorisationTravail autorisationTravail;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "demandeur_id", nullable = false)
    @ToString.Exclude
    private Utilisateur demandeur; // CEEE

    @Column(nullable = false)
    private LocalDateTime dateDemande;

    @Column(nullable = false)
    private LocalDateTime dateFinInitiale;

    @Column(nullable = false)
    private LocalDateTime nouvelleDateFin;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String motif;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private StatutReconduction statut = StatutReconduction.REQUESTED;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "decision_par_id")
    @ToString.Exclude
    private Utilisateur decisionPar; // HMEP (Responsable OCP)

    private LocalDateTime dateDecision;

    @Column(columnDefinition = "TEXT")
    private String motifRefus;

    @Column(columnDefinition = "TEXT")
    private String commentaire;

    @Column(name = "analyse_ia_json", columnDefinition = "TEXT")
    private String analyseIaJson;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;
}
