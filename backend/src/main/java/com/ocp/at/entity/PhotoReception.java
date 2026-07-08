package com.ocp.at.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

/**
 * Entité représentant une photo de réception des travaux.
 */
@Entity
@Table(name = "photos_reception")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PhotoReception {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(nullable = false, length = 255)
    private String nom;

    @Column(nullable = false, length = 500)
    private String path;

    private Long taille;

    @Column(length = 100)
    private String mimeType;

    private Integer ordre;

    @Column(columnDefinition = "TEXT")
    private String legende;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reception_travaux_id", nullable = false)
    private ReceptionTravaux receptionTravaux;
}
