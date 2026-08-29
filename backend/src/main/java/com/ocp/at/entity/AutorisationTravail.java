package com.ocp.at.entity;

import com.ocp.at.entity.enums.EtatVerrou;
import com.ocp.at.entity.enums.StatutAT;
import com.ocp.at.entity.enums.TypeDocumentSource;

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
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString
public class AutorisationTravail {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @EqualsAndHashCode.Include
    private String id;

    @Column(nullable = false)
    private String objet;

    private String descriptionTravaux;

    private LocalDate dateDebut;

    private LocalDate dateFin;

    private LocalTime heureDebut;

    private LocalTime heureFin;

    private LocalDateTime dateReceptionCeee;

    @Column(name = "date_demarrage")
    private LocalDateTime dateDemarrage;

    @Column(name = "date_fin_reelle")
    private LocalDateTime dateFinReelle;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ceee_id")
    @ToString.Exclude
    private Utilisateur ceee;

    @OneToMany(mappedBy = "autorisationTravail", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @BatchSize(size = 50)
    @Builder.Default
    @ToString.Exclude
    private List<Reconduction> reconductions = new ArrayList<>();

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

    /**
     * Statut du workflow conforme au Standard S-HSE-SEC-31 §7.
     *
     * Reflète fidèlement les 9 étapes du logigramme officiel :
     * DEMANDE_CREEE → VISITE_REALISEE → AT_REDIGEE → INTERVENTION_EN_COURS
     * → AT_RECONDUITE → FIN_TRAVAUX_DECLAREE → TRAVAUX_RECEPTIONES → ARCHIVEE
     *
     * Distinct du champ 'statut' (legacy) qui est conservé pour la rétrocompatibilité.
     * Les nouvelles AT doivent utiliser ce champ pour les transitions de workflow.
     * Mappé automatiquement depuis 'statut' lors de la migration V21.
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "statut_workflow")
    private StatutAT statutWorkflow;


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
    @Enumerated(EnumType.STRING)
    @Column(name = "type_document_source")
    private TypeDocumentSource typeDocumentSource;

    @Column(name = "numero_document_source", length = 50)
    private String numeroDocumentSource;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "di_id")
    @ToString.Exclude
    private DemandeIntervention demandeIntervention;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ot_id")
    @ToString.Exclude
    private OrdreTravail ordreTravail;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "bt_id")
    @ToString.Exclude
    private BonTravail bonTravail;

    @OneToMany(mappedBy = "autorisationTravail", fetch = FetchType.LAZY)
    @BatchSize(size = 50)
    @Builder.Default
    @ToString.Exclude
    private List<Visa> visas = new ArrayList<>();

    @OneToMany(mappedBy = "autorisationTravail", fetch = FetchType.LAZY)
    @BatchSize(size = 50)
    @Builder.Default
    @ToString.Exclude
    private List<Permis> permis = new ArrayList<>();

    @OneToMany(mappedBy = "autorisationTravail", fetch = FetchType.LAZY)
    @BatchSize(size = 50)
    @Builder.Default
    @ToString.Exclude
    private List<HistoriqueAT> historiques = new ArrayList<>();

    @OneToOne(mappedBy = "autorisationTravail", fetch = FetchType.LAZY)
    @ToString.Exclude
    private ReceptionTravaux receptionTravaux;

    /**
     * Zone/Service Propriétaire (P) - l'entité responsable de l'installation où se déroule l'intervention.
     * Référence la même table Zone que zoneExecutante (P et E sont le même type d'objet,
     * des rôles différents sur la même AT).
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "zone_proprietaire_id")
    @ToString.Exclude
    private Zone zoneProprietaire;

    /**
     * Zone/Service Exécutant (E) - l'entité qui intervient dans le périmètre de P.
     * Peut être le même Service/Zone que zoneProprietaire sur une autre AT (relation P/E contextuelle).
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "zone_executante_id")
    @ToString.Exclude
    private Zone zoneExecutante;

    // --- Champs spécifiques du formulaire (PDF) ---
    private String servicesIntervenants;
    private String entreprisesIntervenantes;
    private String mesuresSecuriteExecutant;

    /** Cases cochées formulaire F-HSE (JSON array d'IDs) - indépendant des ManyToMany */
    @Column(name = "form_risques_ids", columnDefinition = "TEXT")
    private String formRisquesIds;

    @Column(name = "form_mesures_ids", columnDefinition = "TEXT")
    private String formMesuresIds;

    @Column(name = "form_epis_ids", columnDefinition = "TEXT")
    private String formEpisIds;

    @Column(name = "form_moyens_ids", columnDefinition = "TEXT")
    private String formMoyensIds;

    @Column(name = "form_permis_ids", columnDefinition = "TEXT")
    private String formPermisIds;

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
