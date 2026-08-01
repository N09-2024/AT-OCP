package com.ocp.at.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import java.time.LocalDateTime;

@Entity
@Table(name = "analyses_ia")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString
public class AnalyseIA {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @EqualsAndHashCode.Include
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
    @ToString.Exclude
    private Permis permis;
}
