package com.ocp.at.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "photos")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Photo {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(nullable = false)
    private String nom;

    @Column(nullable = false)
    private String path;

    private String typeMime;

    private Long taille;

    private LocalDateTime dateCreation;

    private Integer ordre;

    private String legende;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "visite_prealable_id")
    private VisitePrealable visitePrealable;
}
