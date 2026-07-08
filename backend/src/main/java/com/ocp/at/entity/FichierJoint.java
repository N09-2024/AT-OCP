package com.ocp.at.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "fichiers_joints")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FichierJoint {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
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
    @JoinColumn(name = "permis_id", unique = true)
    private Permis permis;
}
