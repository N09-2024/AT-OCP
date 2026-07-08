package com.ocp.at.entity;

import com.ocp.at.entity.enums.NiveauIntervention;
import com.ocp.at.entity.enums.StatutDocument;
import com.ocp.at.entity.enums.TypeIntervention;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "ordres_travail")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrdreTravail {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(nullable = false, unique = true, length = 30)
    private String numero; // OT-2026-000001

    @Column(nullable = false)
    private String objet;

    @Column(columnDefinition = "TEXT")
    private String description;

    @CreationTimestamp
    private LocalDateTime dateCreation;

    private LocalDateTime dateExecution;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private StatutDocument statut = StatutDocument.BROUILLON;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TypeIntervention typeIntervention;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private NiveauIntervention niveauIntervention;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "demandeur_id")
    private Utilisateur demandeur;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "installation_id")
    private Installation installation;

    /**
     * Lien vers la visite préalable.
     * La VisitePrealable est attachée au document source, pas à l'AT.
     */
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "visite_prealable_id", unique = true)
    private VisitePrealable visitePrealable;
}
