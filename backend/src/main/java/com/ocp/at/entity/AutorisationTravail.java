package com.ocp.at.entity;

import com.ocp.at.entity.enums.EtatVerrou;
import com.ocp.at.entity.enums.StatutAT;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Entity
@Table(name = "autorisations_travail")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AutorisationTravail {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(nullable = false)
    private String objet;

    private String descriptionTravaux;

    private LocalDate dateDebut;

    private LocalDate dateFin;

    private LocalTime heureDebut;

    private LocalTime heureFin;

    @Column(nullable = false, unique = true, length = 30)
    private String numero; // AT-2026-000001 (futur)

    @CreationTimestamp
    private LocalDateTime dateCreation;

    @UpdateTimestamp
    private LocalDateTime dateModification;

    @Builder.Default
    private Integer version = 1;

    @Enumerated(EnumType.STRING)
    private StatutAT statut;

    @Enumerated(EnumType.STRING)
    private EtatVerrou etatVerrou;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "proprietaire_brouillon_id")
    private Utilisateur proprietaireBrouillon;

    private LocalDateTime datePriseVerrou;

    private LocalDateTime dateLiberationVerrou;

    /**
     * L'AT référence son document source (exactement l'un des trois).
     * La règle "un seul document par AT" est garantie par la contrainte UNIQUE sur chaque colonne.
     * La VisitePrealable et l'AnalyseRisque sont accessibles via le document source.
     */
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "di_id", unique = true)
    private DemandeIntervention demandeIntervention;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ot_id", unique = true)
    private OrdreTravail ordreTravail;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "bt_id", unique = true)
    private BonTravail bonTravail;
}
