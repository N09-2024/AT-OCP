package com.ocp.at.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

/**
 * Entité représentant l'historique des actions sur une réception des travaux.
 */
@Entity
@Table(name = "historiques_reception")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HistoriqueReception {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @CreationTimestamp
    @Column(nullable = false)
    private LocalDateTime dateAction;

    @Column(nullable = false, length = 100)
    private String action;

    @Column(columnDefinition = "TEXT")
    private String commentaire;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "utilisateur_id")
    private Utilisateur utilisateur;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reception_travaux_id", nullable = false)
    private ReceptionTravaux receptionTravaux;
}
