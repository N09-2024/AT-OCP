package com.ocp.at.entity;

import com.ocp.at.entity.enums.StatutVisa;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "visas")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Visa {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    private LocalDateTime dateVisa;

    private LocalDateTime dateSignature;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatutVisa statut;

    private String commentaire;

    private Integer ordre;

    // Signature manuscrite électronique
    private String signaturePath;

    /** SHA-256 du fichier PNG - jamais retourné en API publique */
    private String signatureHash;

    private String adresseIP;

    private String navigateur;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "utilisateur_id", nullable = false)
    private Utilisateur utilisateur;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "at_id", nullable = false)
    private AutorisationTravail autorisationTravail;
}

