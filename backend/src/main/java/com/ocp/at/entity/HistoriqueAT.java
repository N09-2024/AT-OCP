package com.ocp.at.entity;

import com.ocp.at.entity.enums.StatutAT;
import com.ocp.at.entity.enums.TypeActionAT;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "historiques_at")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HistoriqueAT {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    private LocalDateTime dateAction;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TypeActionAT action;

    @Enumerated(EnumType.STRING)
    private StatutAT ancienStatut;

    @Enumerated(EnumType.STRING)
    private StatutAT nouveauStatut;

    private String commentaire;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "utilisateur_id")
    private Utilisateur utilisateur;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "at_id", nullable = false)
    private AutorisationTravail autorisationTravail;
}
