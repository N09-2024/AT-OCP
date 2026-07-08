package com.ocp.at.entity;

import com.ocp.at.entity.enums.StatutPermis;
import com.ocp.at.entity.enums.TypePermis;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "permis")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Permis {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TypePermis type;

    private String numero;

    private LocalDate dateEmission;

    private LocalDate dateExpiration;

    @Enumerated(EnumType.STRING)
    private StatutPermis statutVerification;

    @OneToOne(mappedBy = "permis", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private AnalyseIA analyseIA;

    @OneToOne(mappedBy = "permis", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private FichierJoint fichierJoint;
    
    private Boolean estObligatoire;
    
    @Column(columnDefinition = "TEXT")
    private String commentaire;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "at_id")
    private AutorisationTravail autorisationTravail;
}
