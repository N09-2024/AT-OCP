package com.ocp.at.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "analyses_ia")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AnalyseIA {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    private LocalDateTime dateAnalyse;

    @Column(columnDefinition = "TEXT")
    private String ocrText;

    private Double tauxConfiance;

    private String resultat;

    @Column(columnDefinition = "TEXT")
    private String commentaireIA;

    private Long tempsExecution; // en millisecondes

    private String modeleUtilise;

    private String versionModele;

    @Column(columnDefinition = "TEXT")
    private String jsonExtraction;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "permis_id", unique = true)
    private Permis permis;
}
