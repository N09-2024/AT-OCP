package com.ocp.at.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;

import java.time.LocalDateTime;
import com.ocp.at.entity.enums.ArchiveStatus;

@Entity
@Table(name = "archives_at")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@SQLDelete(sql = "UPDATE archives_at SET deleted = true WHERE id = ?")
@Where(clause = "deleted = false")
public class ArchiveAT {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(nullable = false, unique = true)
    private String numeroArchive;

    @Column(nullable = false)
    private Integer version;

    @Column(nullable = false)
    private LocalDateTime dateArchivage;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "archive_par_id")
    private Utilisateur archivePar;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "autorisation_travail_id", unique = true)
    private AutorisationTravail autorisationTravail;

    @Column(nullable = false)
    private String pathPdf;

    @Column(nullable = false, length = 64)
    private String hashSHA256;

    @Column(nullable = false)
    private Long taille;

    @Column(nullable = false, length = 100)
    private String mimeType;

    @Column(nullable = false)
    private String qrCodePath;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private ArchiveStatus archiveStatus;

    @Column(columnDefinition = "TEXT")
    private String commentaire;

    @Column(nullable = false)
    @Builder.Default
    private Boolean deleted = false;
}