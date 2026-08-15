package com.ocp.at.entity;

import com.ocp.at.entity.enums.StatutPermisDocument;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

/**
 * Document physique (photo/scan) d un permis de travail coché en section E du formulaire F-HSE-SEC-31-04.
 * Chaque type de permis coché par le CEEP génère un enregistrement.
 * L agent IA Gemini analyse le fichier et produit la décision VALIDE/REJETE.
 */
@Entity
@Table(name = "permis_documents")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class PermisDocument {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "autorisation_travail_id", nullable = false)
    private AutorisationTravail autorisationTravail;

    /** Type de permis attendu tel que coché en section E */
    @Column(name = "type_permis_attendu", nullable = false, length = 80)
    private String typePermisAttendu;

    @Column(name = "file_path")
    private String filePath;

    @Column(name = "file_original_name")
    private String fileOriginalName;

    @Column(name = "file_content_type", length = 60)
    private String fileContentType;

    @Enumerated(EnumType.STRING)
    @Column(name = "statut", nullable = false, length = 30)
    @Builder.Default
    private StatutPermisDocument statut = StatutPermisDocument.EN_ATTENTE_UPLOAD;

    @CreationTimestamp
    @Column(name = "date_upload")
    private LocalDateTime dateUpload;

    @Column(name = "date_analyse")
    private LocalDateTime dateAnalyse;

    // -- Champs extraits par Gemini --

    @Column(name = "type_extrait", length = 120)
    private String typeExtrait;

    @Column(name = "date_debut_extrait", length = 20)
    private String dateDebutExtrait;

    @Column(name = "date_fin_extrait", length = 20)
    private String dateFinExtrait;

    @Column(name = "responsables_extraits", columnDefinition = "TEXT")
    private String responsablesExtraits;

    @Column(name = "motif_rejet", columnDefinition = "TEXT")
    private String motifRejet;

    @Column(name = "score_confiance")
    private Double scoreConfiance;

    @Column(name = "commentaire_ia", columnDefinition = "TEXT")
    private String commentaireIA;
}
