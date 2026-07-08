package com.ocp.at.entity;

import jakarta.persistence.*;
import lombok.*;

/**
 * Représente la remise en état du site après les travaux.
 */
@Entity
@Table(name = "remises_etat")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RemiseEtat {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reception_id", nullable = false, unique = true)
    private ReceptionTravaux receptionTravaux;

    @Column(nullable = false)
    @Builder.Default
    private Boolean zoneNettoyee = false;

    @Column(nullable = false)
    @Builder.Default
    private Boolean materielRetire = false;

    @Column(nullable = false)
    @Builder.Default
    private Boolean protectionsRetirees = false;

    @Column(nullable = false)
    @Builder.Default
    private Boolean consignationRetiree = false;

    @Column(columnDefinition = "TEXT")
    private String commentaire;
}
