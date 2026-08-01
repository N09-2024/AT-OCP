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
@Table(name = "fichiers_joints")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString
public class FichierJoint {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @EqualsAndHashCode.Include
    private String id;

    @Column(nullable = false)
    private String nom;

    @Column(nullable = false)
    private String path;

    private String type;

    private Long taille;

    private LocalDateTime dateImport;

    private String hashSHA256;

    private String uploadedBy;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "permis_id")
    @ToString.Exclude
    private Permis permis;
}
