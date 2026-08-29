package com.ocp.at.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Entité représentant la réception des travaux dans le workflow OCP.
 * Une seule réception est possible par Autorisation de Travail.
 */
@Entity
@Table(name = "receptions_travaux")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReceptionTravaux {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "autorisation_travail_id", nullable = false, unique = true)
    private AutorisationTravail autorisationTravail;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "responsable_id")
    private Utilisateur responsable;

    private LocalDateTime dateReception;

    private LocalDateTime dateDebutTravauxReelle;

    private LocalDateTime dateFinTravauxReelle;

    @Column(columnDefinition = "TEXT")
    private String travauxRealises;

    @Column(nullable = false)
    @Builder.Default
    private Boolean travauxConformes = false;

    @Column(nullable = false)
    @Builder.Default
    private Boolean equipementRemisEnService = false;

    @Column(nullable = false)
    @Builder.Default
    private Boolean zoneNettoyee = false;

    @Column(nullable = false)
    @Builder.Default
    private Boolean consignationRetiree = false;

    @Column(nullable = false)
    @Builder.Default
    private Boolean essaisEffectues = false;

    @Column(columnDefinition = "TEXT")
    private String resultatEssais;

    @Column(columnDefinition = "TEXT")
    private String observations;

    @Column(columnDefinition = "TEXT")
    private String commentaireResponsable;

    // Signature manuscrite du responsable (stockée via StorageService)
    private String signaturePath;

    private LocalDateTime signatureDate;

    private String signatureBy;

    // Signature responsable pour clôture
    private String signatureResponsable;

    private LocalDateTime dateSignature;

    // Champs additionnels pour conformité
    private Boolean validee;

    private Boolean essaisConformes;

    private Boolean installationRemiseEnEtat;

    @Enumerated(EnumType.STRING)
    @Column(name = "resultat_reception", length = 30)
    @Builder.Default
    private com.ocp.at.entity.enums.ResultatReception resultatReception = com.ocp.at.entity.enums.ResultatReception.CONFORME;

    @Column(name = "reserves_description", columnDefinition = "TEXT")
    private String reservesDescription;

    @Column(name = "actions_correctives", columnDefinition = "TEXT")
    private String actionsCorrectives;

    @Column(name = "reception_conjointe_validee")
    @Builder.Default
    private Boolean receptionConjointeValidee = false;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "receptionTravaux", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<PhotoReception> photos = new ArrayList<>();

    @OneToMany(mappedBy = "receptionTravaux", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<HistoriqueReception> historiques = new ArrayList<>();
}
