package com.ocp.at.entity;

import com.ocp.at.entity.enums.EtatVerrou;
import com.ocp.at.entity.enums.StatutAT;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.BatchSize;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

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

    @OneToMany(mappedBy = "autorisationTravail", fetch = FetchType.LAZY)
    @BatchSize(size = 50)
    @Builder.Default
    private List<Visa> visas = new ArrayList<>();

    @OneToMany(mappedBy = "autorisationTravail", fetch = FetchType.LAZY)
    @BatchSize(size = 50)
    @Builder.Default
    private List<Permis> permis = new ArrayList<>();

    @OneToMany(mappedBy = "autorisationTravail", fetch = FetchType.LAZY)
    @BatchSize(size = 50)
    @Builder.Default
    private List<HistoriqueAT> historiques = new ArrayList<>();

    @OneToOne(mappedBy = "autorisationTravail", fetch = FetchType.LAZY)
    private ReceptionTravaux receptionTravaux;

    // --- Champs spécifiques du formulaire (PDF) ---
    private String servicesIntervenants;
    private String entreprisesIntervenantes;
    private String mesuresSecuriteExecutant;

    @ManyToMany
    @JoinTable(
        name = "at_risques",
        joinColumns = @JoinColumn(name = "at_id"),
        inverseJoinColumns = @JoinColumn(name = "risque_id")
    )
    @Builder.Default
    private List<Risque> risques = new ArrayList<>();

    @ManyToMany
    @JoinTable(
        name = "at_mesures",
        joinColumns = @JoinColumn(name = "at_id"),
        inverseJoinColumns = @JoinColumn(name = "mesure_id")
    )
    @Builder.Default
    private List<MesurePreparation> mesures = new ArrayList<>();

    @ManyToMany
    @JoinTable(
        name = "at_epis",
        joinColumns = @JoinColumn(name = "at_id"),
        inverseJoinColumns = @JoinColumn(name = "epi_id")
    )
    @Builder.Default
    private List<EPI> epis = new ArrayList<>();

    @ManyToMany
    @JoinTable(
        name = "at_moyens_acces",
        joinColumns = @JoinColumn(name = "at_id"),
        inverseJoinColumns = @JoinColumn(name = "moyen_id")
    )
    @Builder.Default
    private List<MoyenAcces> moyensAcces = new ArrayList<>();
}
