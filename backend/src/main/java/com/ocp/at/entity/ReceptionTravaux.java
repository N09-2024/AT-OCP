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

    private LocalDateTime dateReception;

    @Column(columnDefinition = "TEXT")
    private String commentaire;

    @Column(nullable = false)
    @Builder.Default
    private Boolean travauxConformes = false;

    @Column(nullable = false)
    @Builder.Default
    private Boolean installationRemiseEnEtat = false;

    @Column(nullable = false)
    @Builder.Default
    private Boolean essaisEffectues = false;

    @Column(nullable = false)
    @Builder.Default
    private Boolean essaisConformes = false;

    private LocalDateTime dateValidation;

    @Column(nullable = false)
    @Builder.Default
    private Boolean validee = false;

    private String createdBy;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "receptionTravaux", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<Essai> essais = new ArrayList<>();

    @OneToOne(mappedBy = "receptionTravaux", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private RemiseEtat remiseEtat;
}
